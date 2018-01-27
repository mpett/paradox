import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

public class BigVectorDistance {
    private static final String FILE_PATH =
            "/Users/martinpettersson/materials/";
    private static final String MATERIAL_IDS
            = "/Users/martinpettersson" +
            "/materials/materials.txt";

    /**
     * Database credentials.
     */
    private static final String OMDB_URL
            = "jdbc:mysql://localhost/omdb";
    private static final String VECTORS_URL
            = "jdbc:mysql://localhost/vectors";
    private static final String DATABASE_USER
            = "root";
    private static final String DATABASE_PASSWORD
            = "man3.pett";

    private static ArrayList<Integer> mId;

    public static void main(String[] args) throws SQLException {
        System.err.println("Hello World");


        try {
            mId = materialIndices(MATERIAL_IDS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            parseMaterials(mId);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //createDatabaseTables();
    }

    private static ArrayList<Integer> materialIndices(String fileName)
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ArrayList<Integer> materialIds = new ArrayList<>();
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            String[] materials = everything.split("\n");
            for (String material_index : materials) {
                materialIds.add(Integer.parseInt(material_index));
            }
        } finally {
            br.close();
        }
        return materialIds;
    }

    private static void createDatabaseTables() throws SQLException {
        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        int numberOfTables = 23;
        for (int index = 0; index < numberOfTables; index++) {
            String updateStatement = "CREATE TABLE dos_vectors_"
                    + index + " (\n" +
                    "    vector_id int,\n" +
                    "    material_id int,\n" +
                    "    cod_id int,\n" +
                    "    parsed_dos_vector text,\n" +
                    "    interpolated_dos_vector text,\n" +
                    "    parsed_energy_vector text,\n" +
                    "    close_materials text,\n" +
                    "    hvb_e double\n" +
                    ");";
            Statement statement = connection.createStatement();
            statement.executeUpdate(updateStatement);
        }
        connection.close();
    }

    private static void
    parseMaterials(ArrayList<Integer> indices) throws IOException, SQLException {
        int numberOfCatches = 0;
        int counter = 0;

        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        int tableIndex = 0;

        for (int materialId : indices) {
            ArrayList<Double> dosVector = new ArrayList<>();
            ArrayList<Double> energyVector = new ArrayList<>();
            String extendedFilePath = FILE_PATH;
            extendedFilePath += materialId + "/DOS/dos.json";

            try {
                BufferedReader br = new BufferedReader(
                        new FileReader(extendedFilePath));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }

                String everything = sb.toString();
                everything = everything.substring(7);
                everything = everything.substring(0,
                        everything.length() - 3);
                String[] vectors = everything.split("dos");
                String dosString = vectors[1].substring(4);
                String energyString = vectors[0]
                        .substring(0, vectors[0].length()-4);
                String[] dosSplit = dosString.split(", ");
                String[] energySplit = energyString.split(", ");

                for (String dos : dosSplit)
                    dosVector.add(Double.parseDouble(dos));
                for (String energy : energySplit)
                    energyVector.add(Double.parseDouble(energy));
                br.close();
            } catch (Exception e) {
                numberOfCatches++;
                continue;
            }
            /*
            Material material =
                    new Material(energyVector, dosVector, materialId);
            materials.put(materialId, material);
            */
            //tmpPrintMaterial(dosVector, energyVector, materialId);
            String dosString = listToString(dosVector);
            String energyString = listToString(energyVector);


            String updateStatement = "INSERT INTO dos_vectors_" + tableIndex + " " +
                    "(cod_id, parsed_dos_vector, parsed_energy_vector)\n" +
                    "VALUES (" + materialId + ", '"+ dosString + "', '" + energyString + "')";

            Statement statement = connection.createStatement();
            statement.executeUpdate(updateStatement);

            counter++;
            if (counter % 100 == 0)
                System.err.println("Parsed " + counter);
            if (counter % 1000 == 0) {
                tableIndex++;
                System.err.println("Updated table index to " + tableIndex);
            }

        }
        System.err.println(
                numberOfCatches + " could not be parsed.");
        connection.close();
    }

    private static String listToString(ArrayList<Double> list) {
        StringBuilder sb = new StringBuilder();
        for (double value : list)
            sb.append(value + " ");
        return sb.toString();
    }

    private static void tmpPrintMaterial(ArrayList<Double> dos,
                                         ArrayList<Double> energy,
                                         int id) {
        System.out.println("----- " + id + " -----");
        StringBuilder sb = new StringBuilder();
        for (double v : dos) {
            sb.append(v + " ");
        }
        System.out.println(sb.toString());
        sb = new StringBuilder();
        for (double v : energy) {
            sb.append(v + " ");
        }
        System.out.println(sb.toString());
        System.out.println("----- " + id + " -----");
    }
}
