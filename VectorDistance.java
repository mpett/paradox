import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

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
        Material m = materials.get(mId.get(0));
        /*double[] y = {-4.1399, -4.1359, -4.1319, -4.1279, -4.1239, -4.1199};
        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        for (double v : y)
            System.err.print(v + " ");
        System.err.println("");
        LinearInterpolator li = new LinearInterpolator();
        PolynomialSplineFunction psf = li.interpolate(x, y);
        for (double xi = 1.0; xi <= 4.0; xi+=1.0) {
            double yi = psf.value(xi);
            System.err.print(yi + " ");
        }*/
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

    public double[] toPrimitive(ArrayList<Double> array) {
        if (array == null) {
            return null;
        } else if (array.size() == 0) {
            return EMPTY_DOUBLE_ARRAY;
        }
        final double[] result = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            result[i] = array.get(i);
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
