import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

public class BigVectorDistance {
    private static final int NUMBER_OF_TABLES = 22;

    private static final String FILE_PATH =
            "/Users/martinpettersson/materials/";
    private static final String MATERIAL_IDS
            = "/Users/martinpettersson" +
            "/materials/materials.txt";

    /**
     * Database credentials.
     */
    private static final String OMDB_URL
            = "jdbc:mysql://localhost/omdb?" +
            "autoReconnect=true&useSSL=false";
    private static final String VECTORS_URL
            = "jdbc:mysql://localhost/vectors?" +
            "autoReconnect=true&useSSL=false";
    private static final String DATABASE_USER
            = "root";
    private static final String DATABASE_PASSWORD
            = "man3.pett";

    private static ArrayList<Integer> mId;

    private static final int NUMBER_OF_INTERPOLATING_POINTS = 1000;

    public static void main(String[] args) throws SQLException,
                                    ClassNotFoundException {
        System.err.println("Hello World");
        //printResultSet();
        findMaterial(7150965);

        /*

        dropAllTables();
        createDatabaseTables();

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

        */


    }

    private static void findMaterial(int codId) throws SQLException {
        for (int tableIndex = 0; tableIndex < NUMBER_OF_TABLES;
                tableIndex++) {
            java.sql.Connection connection
                    = DriverManager.getConnection
                    (VECTORS_URL,
                            DATABASE_USER,
                                 DATABASE_PASSWORD);
            String query = "Select interpolated_dos_vector" +
                            ", interpolated_energy_vector " +
                            "from dos_vectors_"
                            + tableIndex +
                            " where cod_id="
                            + codId;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                String dosVector = resultSet.getString(1);
                String energyVector = resultSet.getString(2);
                String[] dosSplit = dosVector.split(" ");
                double[] dos = new double[dosSplit.length];
                String[] energySplit = energyVector.split(" ");
                double[] energy = new double[energySplit.length];
                if (dos.length != energy.length) {
                    System.err.println("Something is wrong with" +
                            " vector lengths.");
                    break;
                }
                int i = 0;
                for (String element : dosSplit) {
                    double value = Double.parseDouble(element);
                    dos[i] = value;
                    i++;
                }
                i = 0;
                for (String element : energySplit) {
                    double value = Double.parseDouble(element);
                    energy[i] = value;
                    i++;
                }
                System.out.println("Found in table " + tableIndex);
                for (double v : dos)
                    System.err.print(v + ", ");
                System.err.println("");
                for (double v : energy)
                    System.err.print(v + ", ");
                System.err.println("");
                System.err.println(dos.length + " " + energy.length);
                break;
            }
            connection.close();
        }
    }

    private static void printResultSet() throws SQLException {
        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        String query = "select cod_id, interpolated_dos_vector, " +
                "interpolated_energy_vector from dos_vectors_18";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        System.err.println("Done");

        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
        connection.close();
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

    private static void dropAllTables() throws SQLException {
        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        int numberOfTables = 23;
        for (int index = 0; index < numberOfTables; index++) {
            String updateStatement = "DROP TABLE dos_vectors_"
                    + index + ";";
            Statement statement = connection.createStatement();
            statement.executeUpdate(updateStatement);
        }
        connection.close();
    }

    private static void createDatabaseTables() throws SQLException {
        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        int numberOfTables = NUMBER_OF_TABLES;
        for (int index = 0; index < numberOfTables; index++) {
            String updateStatement = "CREATE TABLE dos_vectors_"
                    + index + " (\n" +
                    "    vector_id int,\n" +
                    "    material_id int,\n" +
                    "    cod_id int,\n" +
                    "    parsed_dos_vector text,\n" +
                    "    interpolated_dos_vector text,\n" +
                    "    parsed_energy_vector text,\n" +
                    "    interpolated_energy_vector text,\n" +
                    "    close_materials text,\n" +
                    "    hvb_e double\n" +
                    ");";
            Statement statement = connection.createStatement();
            statement.executeUpdate(updateStatement);
        }
        connection.close();
    }

    private static void
    parseMaterials(ArrayList<Integer> indices) throws IOException,
            SQLException, ClassNotFoundException {
        int numberOfCatches = 0;
        int counter = 1;

        java.sql.Connection connection
                = DriverManager.getConnection
                (VECTORS_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        int tableIndex = 0;
        int missingHvbValues = 0;

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

            double hvb_e = 0.0;

            try {
                hvb_e = getHVBFromDatabase(materialId);
            } catch (Exception e) {
                missingHvbValues++;
            }

            double maximum = hvb_e;
            double minimum = hvb_e - 2.0;

            ArrayList<Double> filteredDos = new ArrayList<>();
            ArrayList<Double> filteredEnergy = new ArrayList<>();

            int i = 0;
            for (double value : energyVector) {
                if (value >= minimum
                        && value <= maximum) {
                    filteredEnergy.add(value);
                    double dosValue = dosVector.get(i);
                    if (dosValue == 0)
                        dosValue++;
                    filteredDos.add(dosValue);
                }
                i++;
            }

            String dosString = listToString(filteredDos);
            String energyString = listToString(filteredEnergy);

            double[] x = toPrimitive(filteredEnergy);
            double[] y = toPrimitive(filteredDos);

            ArrayList<Double> interpolatedDos = new ArrayList<>();
            ArrayList<Double> interpolatedEnergy = new ArrayList<>();

            LinearInterpolator li
                    = new LinearInterpolator();
            try {
                PolynomialSplineFunction psf
                        = li.interpolate(x, y);
                double maxEnergy = 0.0;
                double minEnergy = 0.0;

                try {
                    maxEnergy = Collections.max(filteredEnergy);
                    minEnergy = Collections.min(filteredEnergy);
                } catch (Exception e) {
                    System.err.println("Something went wrong for "
                            + materialId + " " +
                            "when collecting max/min energies");
                }

                double energyIncrementor = (maxEnergy - minEnergy) / 1001.0;
                double energyValue = minEnergy;

                PolynomialSplineFunction interpolatingFunction = psf;

                for (int j = 0; j < NUMBER_OF_INTERPOLATING_POINTS; j++) {
                    energyValue += energyIncrementor;
                    double interpolatedDosValue = 0.0;
                    try {
                        interpolatedDosValue
                                = interpolatingFunction
                                .value(energyValue);
                        if (interpolatedDosValue == 0.0)
                            interpolatedDosValue = 1.0;
                    } catch (Exception e) {
                        System.err.println("Interpolated value could" +
                                " not be calculated for "
                                + materialId
                                + " at index " + j);
                    }
                    interpolatedDos.add(interpolatedDosValue);
                    interpolatedEnergy.add(energyValue);

                }

            } catch (Exception e) {
                System.err.println("Material "
                        + materialId + " " +
                        "has too few points to be interpolated.");
            }

            String intDosString = listToString(interpolatedDos);
            String intEnergyString = listToString(interpolatedEnergy);

            String updateStatement = "INSERT INTO dos_vectors_" + tableIndex + " " +
                    "(cod_id, parsed_dos_vector, parsed_energy_vector, hvb_e" +
                    ", interpolated_dos_vector, interpolated_energy_vector)\n" +
                    "VALUES (" + materialId + ", '"+ dosString + "', '"
                    + energyString + "', " + hvb_e + ", '" + intDosString + "'," +
                    "'" + intEnergyString+ "')";

            Statement statement = connection.createStatement();
            statement.executeUpdate(updateStatement);

            counter++;
            if (counter % 100 == 0) {
                System.err.println("Parsed " + counter);
                System.err.println(missingHvbValues + " HVB_E values are missing.");
            }
            if (counter % 1000 == 0) {
                tableIndex++;
                System.err.println("Updated table index to " + tableIndex);
            }

        }
        System.err.println(
                numberOfCatches + " could not be parsed.");
        connection.close();
    }

    private static double[] toPrimitive(ArrayList<Double> array) {
        double[] emptyArray = {};
        if (array == null) {
            return null;
        } else if (array.size() == 0) {
            return emptyArray;
        }
        final double[] result = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i);
        }
        return result;
    }

    private static String listToString(ArrayList<Double> list) {
        StringBuilder sb = new StringBuilder();
        for (double value : list)
            sb.append(value + " ");
        return sb.toString();
    }

    private static double getHVBFromDatabase(int codCode)
            throws IOException, ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");

        java.sql.Connection connection
                = DriverManager.getConnection
                (OMDB_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);

        String idQuery = "select material_id " +
                "from materials " +
                "where _cod_database_code="
                + codCode;
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(idQuery);

        String materialId = "";
        while (rs.next()) {
            String em =
                    rs.getString
                            ("material_id");
            materialId = em;
        }

        String hvbQuery = "select HVB_E " +
                "from materials_dft " +
                "where material_id="
                + materialId;
        statement = connection.createStatement();
        rs = statement.executeQuery(hvbQuery);

        String hvb = "";
        while (rs.next()) {
            String em =
                    rs.getString
                            ("HVB_E");
            hvb = em;
        }
        connection.close();
        double resultingHVB = Double.parseDouble(hvb);
        return resultingHVB;
    }
}
