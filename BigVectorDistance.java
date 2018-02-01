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

    private static final boolean COSINE = false;
    private static final boolean EUCLIDEAN = true;
    private static final boolean REBUILD_DATABASE = false;

    private static PrintWriter simWriter;

    static {
        try {
            simWriter =
                    new PrintWriter(
                            "similarities_euc.txt",
                            "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws SQLException,
            ClassNotFoundException, IOException {
        if (REBUILD_DATABASE) {
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
        }


        try {
            mId = materialIndices(MATERIAL_IDS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int c = 0;
        for (int id : mId) {
            c++;
            if (c == 10) break;
            try {
                Map<Integer, Double> results
                        = calculateDistances(id);
                writeForPlot(results, id);
                System.err.println("Done with material " + c);
            } catch (NullPointerException e) {
                c--;
                System.err.println("Nullpointer for " + id);
                continue;
            }
        }

        simWriter.close();
    }

    private static void populateSimilarities() throws IOException, SQLException {
        java.sql.Connection connection
                = DriverManager.getConnection
                (OMDB_URL,
                        DATABASE_USER,
                        DATABASE_PASSWORD);
        BufferedReader br = new BufferedReader(
                new FileReader("similarities.txt"));

        String everything;

        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } finally {
            br.close();
        }

        String query;
        String[] elements = everything.split("\n");
        for (String e : elements) {
            System.err.println(e);
            String[] lines = e.split(" ");
            query = "INSERT INTO similarities (reference_id, cod1_cos, cod2_cos, " +
                    "cod3_cos, cod4_cos, cod5_cos)" +
                    " VALUES (" + lines[0] + ", " + lines[2] + ", " + lines[3] + ", "
                    + lines[4] + ", " + lines[5] + ", " + lines[6] + ")";
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        }

        BufferedReader br2 =
                new BufferedReader(
                        new FileReader("similarities_euc.txt"));

        try {
            StringBuilder sb = new StringBuilder();
            String line = br2.readLine();
            System.err.println(line);

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br2.readLine();
            }
            everything = sb.toString();
        } finally {
            br2.close();
        }

        elements = everything.split("\n");

        for (String e : elements) {
            System.err.println(e);
            String[] lines = e.split(" ");
            query = "UPDATE similarities SET cod1_euc="+lines[2]
                    + ", cod2_euc="+lines[3] + ", " +
                    "cod3_euc="+lines[4] + ", cod4_euc="+lines[5]
                    +", cod5_euc=" +lines[6] +
                    " WHERE reference_id=" + lines[0];
            Statement statement = connection.createStatement();
            System.err.println(query);
            statement.executeUpdate(query);
        }

        connection.close();
    }

    private static MaterialObject findMaterial(int codId) throws SQLException {
        MaterialObject result = null;
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
                MaterialObject m = new MaterialObject(dos, energy);
                result = m;
                break;
            }
            connection.close();
        }
        return result;
    }

    private static void writeForPlot(Map<Integer, Double> results,
                                     int referenceCod)
            throws SQLException, IOException {
        PrintWriter writer
                = new PrintWriter
                ("vectors.txt", "UTF-8");
        MaterialObject reference =
                findMaterial(referenceCod);
        double[] energy = reference.energy;
        double[] dos = reference.dos;
        for (double v : energy)
            writer.print(v + " ");
        writer.println();
        for(double v : dos)
            writer.print(v + " ");
        writer.println();
        Set<Integer> keySet =
                results.keySet();
        System.err.println("Wrote first line");
        int c = 0;
        for (int key : keySet) {
            c++;
            if (c==20) break;
            System.err.println("Wrote "
                    + c + " lines.");
            MaterialObject m =
                    findMaterial(key);
            energy = m.energy;
            dos = m.dos;
            for (double v : energy)
                writer.print(v + " ");
            writer.println();
            for(double v : dos)
                writer.print(v + " ");
            writer.println();
        }
        writer.close();
    }

    private static double euclideanSimilarity(double[] vectorA,
                                              double[] vectorB) {
        EuclideanDistance euclideanDistance
                = new EuclideanDistance();
        double distance = euclideanDistance
                .compute
                        (vectorA, vectorB);
        return distance;
    }

    private static double cosineSimilarity(double[] vectorA,
                                           double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        double result = Math.acos(dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB)));

        return result;
    }

    private static Map<Integer, Double> calculateDistances(int referenceCodId)
            throws SQLException, FileNotFoundException, UnsupportedEncodingException {
        MaterialObject referenceMaterial = findMaterial(referenceCodId);
        HashMap<Integer, Double> similarities = new HashMap<>();

        for (int tableIndex = 0; tableIndex < NUMBER_OF_TABLES; tableIndex++) {
            java.sql.Connection connection
                    = DriverManager.getConnection
                    (VECTORS_URL,
                            DATABASE_USER,
                            DATABASE_PASSWORD);
            String query = "select cod_id, interpolated_dos_vector, " +
                    "interpolated_energy_vector from dos_vectors_" + tableIndex;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            System.err.print(tableIndex + " ");
            HashMap<Integer, Double> tmpSimilarities = new HashMap<>();

            while (resultSet.next()) {
                int codId = resultSet.getInt(1);
                String dosVector = resultSet.getString(2);
                String energyVector = resultSet.getString(3);
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
                try {
                    for (String element : dosSplit) {
                        double value = Double.parseDouble(element);
                        dos[i] = value;
                        i++;
                    }
                } catch (NumberFormatException e) {
                    String removeQuery = "delete from dos_vectors_"
                            + tableIndex + " where cod_id=" + codId;
                    Statement removeStatement = connection.createStatement();
                    removeStatement.executeUpdate(removeQuery);
                    System.err.println("Removed " + codId + " due to blank element.");
                    continue;
                }
                i = 0;
                for (String element : energySplit) {
                    double value = Double.parseDouble(element);
                    energy[i] = value;
                    i++;
                }

                double cosineDistance = 0.0;

                if (COSINE) {
                    cosineDistance
                            = cosineSimilarity(referenceMaterial.dos, dos);
                } else if (EUCLIDEAN) {
                    cosineDistance
                            = euclideanSimilarity(referenceMaterial.dos, dos);
                }

                if (Double.isNaN(cosineDistance)) {
                    cosineDistance = 0.0;
                }

                tmpSimilarities.put(codId, cosineDistance);
            }
            connection.close();
            Map<Integer, Double> sortedTmpSimilarities = sortByValue(tmpSimilarities);
            int c = 0;
            Set<Integer> keySet = sortedTmpSimilarities.keySet();
            for (int key : keySet) {
                c++;
                if (c==20) break;
                similarities.put(key, sortedTmpSimilarities.get(key));
            }
        }
        System.err.println("");
        Map<Integer, Double> result = sortByValue(similarities);
        Set<Integer> keySet = result.keySet();

        simWriter.write(referenceCodId + " ");

        int c = 0;
        for (int key : keySet) {
            c++;
            if (c==20) break;
            System.err.println(referenceCodId +
                    " dist " + key + " = " + similarities.get(key));
            simWriter.write( key + " ");
        }
        simWriter.write("\n");
        System.err.println("done");
        return result;
    }

    private static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry
                        .comparingByValue(
                               /* Collections
                                        .reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
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
        int numberOfTables = NUMBER_OF_TABLES;
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
                            interpolatedDosValue = 0.0;
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

class MaterialObject {
    public double[] dos;
    public double[] energy;

    public MaterialObject(double[] dos, double[] energy) {
        this.dos = dos;
        this.energy = energy;
    }
}
