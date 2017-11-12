import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class VectorDistance {
    private static final String FILE_PATH =
            "/Users/martinpettersson/diraclaravel" +
                    "/data/materials/_cod_database_code" +
                    "/";
    private static final String MATERIAL_IDS
            = "/Users/martinpettersson" +
            "/diraclaravel/data/materials" +
            "/_cod_database_code/materials.txt";

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        ArrayList<Integer> mId = materialIndices(MATERIAL_IDS);
        HashMap<Integer, Material> materials = parseMaterials(mId);
        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Parse time: " + elapsedTime);
        Set<Integer> keys = materials.keySet();
        System.err.println("Number of materials in hash map: " + keys.size());
    }

    private static HashMap<Integer, Material>
            parseMaterials(ArrayList<Integer> indices) throws IOException {
        HashMap<Integer, Material> materials = new HashMap<>();
        int numberOfCatches = 0;

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
                for (String dos : dosSplit) {
                    dosVector.add(Double.parseDouble(dos));
                }
                for (String energy : energySplit) {
                    energyVector.add(Double.parseDouble(energy));
                }
                br.close();
            } catch (Exception e) {
                numberOfCatches++;
                continue;
            }
            Material material = new Material(energyVector, dosVector, materialId);

            materials.put(materialId, material);
        }
        System.err.println(numberOfCatches + " could not be parsed.");

        return materials;
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

    private static double linearInterpolate(double after, double before,
                                            double atPoint) {
        return before + (after - before) * atPoint;
    }

    private double[] interpolateArray(double[] data, int fitCount) {
        double[] newData = new double[fitCount];
        double springFactor = (double) (data.length - 1)
                                / (double) (fitCount - 1);
        newData[0] = data[0];
        for (int i = 1; i < fitCount - 1; i++) {
            double tmp = (double) (i) * springFactor;
            double before = Math.floor(tmp);
            double after = Math.ceil(tmp);
            double atPoint = tmp - before;
            newData[i] = linearInterpolate(after, before, atPoint);
        }
        newData[fitCount - 1] = data[data.length - 1];
        return newData;
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
        return dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

class Material {
    private ArrayList<Double> energy;
    private ArrayList<Double> dos;
    private int materialId;
    private final double[] EMPTY_DOUBLE_ARRAY = {};

    public Material(ArrayList<Double> energy,
                    ArrayList<Double> dos,
                    int materialId) {
        this.energy = energy;
        this.dos = dos;
        this.materialId = materialId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public ArrayList<Double> getDos() {
        return dos;
    }

    public ArrayList<Double> getEnergy() {
        return energy;
    }

    public double[] toPrimitive(Double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        final double[] result = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i].doubleValue();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Material ID: ");
        stringBuilder.append(materialId);
        stringBuilder.append("\n");

        for (Double value : energy) {
            stringBuilder.append(value);
            stringBuilder.append(" ");
        }

        stringBuilder.append("\n");

        for (Double value : dos) {
            stringBuilder.append(value);
            stringBuilder.append(" ");
        }

        return stringBuilder.toString();
    }
}
