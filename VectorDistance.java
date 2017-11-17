import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
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
    private static ArrayList<Integer> mId;
    private static HashMap<Integer, Material> materials;
    private static final double MINIMUM_ENERGY_THRESHOLD = -1.0;
    private static final double MAXIMUM_ENERGY_THRESHOLD = 1.0;

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0L;
        mId = materialIndices(MATERIAL_IDS);
        materials = parseMaterials(mId);
        Set<Integer> keys = materials.keySet();
        System.err.println
                ("Number of materials in hash map: " + keys.size());
        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Time after parse: " + elapsedTime);

        unsortEnergy();
        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Time after unsorting: " + elapsedTime);

        for (int id : mId) {
            Material m = materials.get(id);
            double[] y = m.toPrimitive(m.getEnergy());
            double[] x = m.toPrimitive(m.getDos());
            SplineInterpolator si = new SplineInterpolator();
            LinearInterpolator li = new LinearInterpolator();
            PolynomialSplineFunction psf = li.interpolate(x, y);
            PolynomialSplineFunction psf2 = si.interpolate(x, y);
            m.setPsf(psf);
            m.setPsf2(psf2);
        }
        
        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Time after interpolation: " + elapsedTime);

        try {
            filterOnEnergyThreshold();
        } catch (Exception e) {
            e.printStackTrace();
        }

        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Time after energy filtering: " + elapsedTime);

        for (int id : mId) {
            Material m = materials.get(id);
            System.err.println(m);
        }

    }

    private static void filterOnEnergyThreshold() throws Exception {
        for (int id : mId) {
            Material m = materials.get(id);
            ArrayList<Double> dos = m.getDos();
            ArrayList<Double> energy = m.getEnergy();
            ArrayList<Double> newDos = new ArrayList<>();
            ArrayList<Double> newEnergy = new ArrayList<>();
            if (dos.size() != energy.size()) {
                System.err.println(
                        "Energy and DOS vector " +
                        "are of different" +
                        "length");
                throw new Exception();
            }
            for (int index = 0; index < energy.size(); index++) {
                double dosValue = dos.get(index);
                double energyValue = energy.get(index);
                if (energyValue >= MINIMUM_ENERGY_THRESHOLD &&
                        energyValue <= MAXIMUM_ENERGY_THRESHOLD) {
                    newDos.add(dosValue);
                    newEnergy.add(energyValue);
                } else {
                    continue;
                }
            }
            if (dos.size() != energy.size()) {
                System.err.println(
                        "Energy and DOS vector " +
                                "are of different" +
                                "length");
                throw new Exception();
            }
            m.setDos(newDos);
            m.setEnergy(newEnergy);
        }
    }

    private static HashMap<Integer, Material>
            parseMaterials(ArrayList<Integer> indices) throws IOException {
        HashMap<Integer, Material> materials = new HashMap<>();
        int numberOfCatches = 0;
        ArrayList<Integer> indicesToBeRemoved = new ArrayList<>();

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
                indicesToBeRemoved.add(materialId);
                continue;
            }
            Material material =
                    new Material(energyVector, dosVector, materialId);

            materials.put(materialId, material);
        }
        System.err.println(numberOfCatches + " could not be parsed.");

        for (int id : indicesToBeRemoved) {
            mId.remove(new Integer(id));
        }

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

    private static void unsortEnergy() {
        for (int materialId : mId) {
            Material m = materials.get(materialId);
            HashMap<Double, Double> dosEnergyMap =
                    new HashMap<>();
            ArrayList<Double> dos = m.getDos();
            ArrayList<Double> energy = m.getEnergy();
            for (int index = 0; index < dos.size(); index++) {
                    dosEnergyMap.put(
                            dos.get(index),
                            energy.get(index));
            }
            Set<Double> dosSet = new HashSet<>();
            dosSet.addAll(dos);
            dos.clear();
            dos.addAll(dosSet);
            Collections.sort(dos);
            ArrayList<Double> unsortedEngergy =
                    new ArrayList<>();
            for (double dosValue : dos) {
                unsortedEngergy
                        .add(dosEnergyMap.get(dosValue));
            }
            m.setEnergy(unsortedEngergy);
            m.setDos(dos);
        }
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
    private PolynomialSplineFunction psf;
    private PolynomialSplineFunction psf2;

    public Material(ArrayList<Double> energy,
                    ArrayList<Double> dos,
                    int materialId) {
        this.energy = energy;
        this.dos = dos;
        this.materialId = materialId;
    }

    public void setPsf2(PolynomialSplineFunction psf2) {
        this.psf2 = psf2;
    }

    public PolynomialSplineFunction getPsf2() {
        return psf2;
    }

    public void setPsf(PolynomialSplineFunction psf) {
        this.psf = psf;
    }

    public PolynomialSplineFunction getPsf() {
        return psf;
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

    public void setEnergy(ArrayList<Double> energy) {
        this.energy = energy;
    }

    public void setDos(ArrayList<Double> dos) {
        this.dos = dos;
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

        stringBuilder.append("\n");
        stringBuilder.append(dos.size() +
                " " + energy.size());

        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
