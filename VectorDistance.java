import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.text.similarity.CosineSimilarity;

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

        // Parsing files
        mId = materialIndices(MATERIAL_IDS);
        materials = parseMaterials(mId);
        Set<Integer> keys = materials.keySet();
        System.err.println
                ("Number of materials in hash map: " + keys.size());
        elapsedTime = (new Date()).getTime() - startTime;
        System.err.println("Time after parse: " + elapsedTime);

        // Filter energies from thresholds
        for (int key : keys) {
            Material m = materials.get(key);
            ArrayList<Double> energy = m.getEnergy();
            ArrayList<Double> dos = m.getDos();
            ArrayList<Double> nE = new ArrayList<>();
            ArrayList<Double> nD = new ArrayList<>();
            int i = 0;
            for (double value : energy) {
                if (value >= MINIMUM_ENERGY_THRESHOLD
                        && value <= MAXIMUM_ENERGY_THRESHOLD) {
                    nE.add(value);
                    nD.add(dos.get(i));
                }
                i++;
            }
            m.setEnergy(nE);
            m.setDos(nD);
        }

        interpolate();

        createInterpolatedDosArray();


        for (int key : keys) {
            Material m = materials.get(key);
            System.err.println(m);
        }

        distances2();

        displayTopCandidatesOnInput();
    }

    private static double cosineSimilarity(Material m1, Material m2) {
        CosineSimilarity dist = new CosineSimilarity();

        String c1 = vectorToString(m1.getEnergy());
        String c2 = vectorToString(m2.getEnergy());

        Map<CharSequence, Integer> leftVector =
                Arrays.stream(c1.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));
        Map<CharSequence, Integer> rightVector =
                Arrays.stream(c2.split(""))
                        .collect(Collectors.toMap(c -> c, c -> 1, Integer::sum));

        return  (1 - dist.cosineSimilarity(leftVector,rightVector));
    }

    private static String vectorToString(ArrayList<Double> vector) {
        StringBuilder stringBuilder = new StringBuilder();
        for (double d : vector) {
            stringBuilder.append(d);
        }
        return stringBuilder.toString();
    }

    private static void displayTopCandidatesOnInput() throws IOException {
        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(System.in));
        while (true) {
            try {
                System.err.print("Would you kindly enter a Material ID: ");
                int id = Integer.parseInt(reader.readLine());
                System.err.println();
                if (id == 0) {
                    reader.close();
                    System.exit(0);
                }
                System.err.println("");
                System.err.println("--- EUCLIDEAN ---");
                System.err.println("");
                Material m = materials.get(id);
                PrintWriter writer = new PrintWriter("vectors.txt", "UTF-8");
                double[] energy = m.toPrimitive(m.getEnergy());
                double[] dos = m.toPrimitive(m.getDos());
                for (double v : energy)
                    writer.print(v + " ");
                writer.println();
                for(double v : dos)
                    writer.print(v + " ");
                writer.println();
                double[] intEnergy = m.toPrimitive(m.getInterpolatedEnergy());
                double[] intDos = m.toPrimitive(m.getInterpolatedDos());
                for (double v : intEnergy)
                    writer.print(v + " ");
                writer.println();
                for (double v : intDos)
                    writer.print(v + " ");
                writer.println();
                Map<Integer, Double> d = topCandidates(m);
                int j = 0;
                for (int i : d.keySet()) {
                    if (j > 20) break;
                    j++;
                    System.err.println(m.getMaterialId() + " dist "
                            + i + " = " + d.get(i));
                    Material dm = materials.get(i);
                    energy = dm.toPrimitive(dm.getEnergy());
                    dos = dm.toPrimitive(dm.getDos());
                    for (double v : energy)
                        writer.print(v + " ");
                    writer.println();
                    for(double v : dos)
                        writer.print(v + " ");
                    writer.println();
                    intEnergy = dm.toPrimitive(dm.getInterpolatedEnergy());
                    intDos = dm.toPrimitive(dm.getInterpolatedDos());
                    for (double v : intEnergy)
                        writer.print(v + " ");
                    writer.println();
                    for (double v : intDos)
                        writer.print(v + " ");
                    writer.println();

                }
                writer.println();
                writer.close();
                System.err.println("");
                System.err.println("--- COSINE ---");
                System.err.println("");

                Map<Integer, Double> c = topCosineCandidates(m);
                int k = 0;
                for (int i : c.keySet()) {
                    if (k > 20) break;
                    k++;
                    System.err.println(m.getMaterialId() + " dist "
                            + i + " = " + c.get(i));
                }
            } catch (Exception e) {
                System.err.println
                        ("Not a valid id among the ones we have.");
                continue;
            }
        }
    }

    private static Map<Integer, Double> topCosineCandidates(Material m) {
        HashMap<Integer, Double> distances = m.getCosineDistances();
        Map<Integer, Double> sortedMap = sortByValue(distances);
        return sortedMap;
    }

    private static Map<Integer, Double> topCandidates(Material m) {
        HashMap<Integer, Double> distances = m.getDistances();
        Map<Integer, Double> sortedMap = sortByValue(distances);
        return sortedMap;
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

    private static void writeVectorsToFile(double[] array, PrintWriter writer) throws IOException {
        for (double value : array) {
            writer.print(value + " ");
        }
        writer.println();
        writer.close();
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

    private static void distances2() {
        for (int id : mId) {
            Material m = materials.get(id);
            for (int id2 : mId) {
                Material m2 = materials.get(id2);
                EuclideanDistance euclideanDistance = new EuclideanDistance();
                double[] firstMaterialDos = m.toPrimitive(m.getInterpolatedDos());
                double[] secondMaterialDos = m2.toPrimitive(m2.getInterpolatedDos());
                double d = euclideanDistance.compute(firstMaterialDos, secondMaterialDos);
                m.setDistances(m2.getMaterialId(), d);
                double cosineDistance = cosineSimilarity(firstMaterialDos, secondMaterialDos);
                m.setCosineDistances(m2.getMaterialId(), cosineDistance);
            }
        }
        System.err.println("Managed to calculate distances.");
    }

    private static void distances() {
        for (int id : mId) {
            Material m = materials.get(id);
            if (!m.hasBeenInterpolated) continue;
            for (int id2 : mId) {
                Material m2 = materials.get(id2);
                if(!m2.hasBeenInterpolated) continue;
                EuclideanDistance eDistance = new EuclideanDistance();
                double[] firstMaterialEnergy = m.toPrimitive(
                        m.getInterpolatedEnergy()
                );
                double[] secondMaterialEnergy = m2.toPrimitive(
                        m2.getInterpolatedEnergy()
                );
                double d = eDistance.compute(firstMaterialEnergy,
                        secondMaterialEnergy);
                m.setDistances(m2.getMaterialId(), d);

                double cosineDistance = cosineSimilarity(m, m2);
                m.setCosineDistances(m2.getMaterialId(), cosineDistance);
            }
        }
    }

    private static void createInterpolatedDosArray() {
        for (int id : mId) {
            Material m = materials.get(id);
            if (!m.hasBeenInterpolated) continue;
            ArrayList<Double> dos = m.getDos();
            ArrayList<Double> energy = m.getEnergy();
            ArrayList<Double> interpolatedDos = new ArrayList<>();
            ArrayList<Double> interpolatedEnergy = new ArrayList<>();
            double maxEnergy;
            double minEnergy;

            try {
                maxEnergy = Collections.max(energy);
                minEnergy = Collections.min(energy);
            } catch (Exception e) {
                System.err.println("Something went wrong for " + m.getMaterialId() + " " +
                        "when collectung max/min energies");
                continue;
            }

            double energyIncrementor = (maxEnergy - minEnergy) / 1001.0;
            double energyValue = minEnergy;
            PolynomialSplineFunction interpolatingFunction = m.getPsf();

            for (int i = 0; i < 1000; i++) {
                energyValue += energyIncrementor;
                double interpolatedDosValue = 0.0;
                try {
                    interpolatedDosValue = interpolatingFunction.value(energyValue);
                    if (interpolatedDosValue == 0.0) {
                        interpolatedDosValue = 1.0;
                    }
                } catch (Exception e) {
                    System.err.println("Interpolated value could" +
                            " not be calculated for "
                            + m.getMaterialId()
                            + " at index " + i);
                }
                interpolatedDos.add(interpolatedDosValue);
                interpolatedEnergy.add(energyValue);

            }
            m.setInterpolatedDos(interpolatedDos);
            m.setInterpolatedEnergy(interpolatedEnergy);
        }
    }

    private static void createInterpolatedEnergyArray() {
        for (int id : mId) {
            Material m = materials.get(id);
            if (!m.hasBeenInterpolated) continue;
            ArrayList<Double> dos = m.getDos();
            ArrayList<Double> energy = m.getEnergy();
            ArrayList<Double> interpolatedEnergy = new ArrayList<>();
            double maxDos;
            double minDos;
            try {
                maxDos = Collections.max(dos);
                minDos = Collections.min(dos);
            } catch (NoSuchElementException e) {
                System.err.println("Material " + m.getMaterialId() + " " +
                        "does not have a maximum or minimum dos value" +
                        "after filtering.");
                continue;
            }

            double dosIncrementor = (maxDos-minDos) / 101.0;
            double dosValue = minDos;
            PolynomialSplineFunction interpolatingFunction =
                    m.getPsf2();
            for (int i = 0 ; i < 100; i++) {
                dosValue += dosIncrementor;
                double interpolatedEnergyValue = 0.0;
                try {
                    interpolatedEnergyValue =
                            interpolatingFunction.value(dosValue);
                } catch (Exception e) {
                    System.err.println("Interpolated value could" +
                            " not be calculated for "
                            + m.getMaterialId()
                            + " at index " + i);
                }
                interpolatedEnergy.add(interpolatedEnergyValue);
            }
            m.setInterpolatedEnergy(interpolatedEnergy);
        }
    }

    private static void interpolate() {
        for (int id : mId) {
            Material m = materials.get(id);
            double[] x = m.toPrimitive(m.getEnergy());
            double[] y = m.toPrimitive(m.getDos());
            SplineInterpolator si = new SplineInterpolator();
            LinearInterpolator li = new LinearInterpolator();
            try {
                PolynomialSplineFunction psf = li.interpolate(x, y);
                PolynomialSplineFunction psf2 = si.interpolate(x, y);
                m.setPsf(psf);
                m.setPsf2(psf2);
                m.hasBeenInterpolated = true;
            } catch (Exception e) {
                System.err.println("Material " + m.getMaterialId() + " " +
                    "has too few points to be interpolated.");
            }
        }
    }

    private static void printAllMaterials() {
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
}

class Material {
    private ArrayList<Double> energy;
    private ArrayList<Double> dos;
    private ArrayList<Double> interpolatedEnergy;
    private ArrayList<Double> interpolatedDos;
    private int materialId;
    private final double[] EMPTY_DOUBLE_ARRAY = {};
    private PolynomialSplineFunction psf;
    private PolynomialSplineFunction psf2;
    private HashMap<Integer, Double> distances;
    private HashMap<Integer, Double> cosineDistances;

    public boolean hasBeenInterpolated = false;

    public Material(ArrayList<Double> energy,
                    ArrayList<Double> dos,
                    int materialId) {
        distances = new HashMap<>();
        cosineDistances = new HashMap<>();
        interpolatedEnergy = new ArrayList<>();
        this.energy = energy;
        this.dos = dos;
        this.materialId = materialId;
    }

    public void setInterpolatedDos(ArrayList<Double> interpolatedDos) {
        this.interpolatedDos = interpolatedDos;
    }

    public ArrayList<Double> getInterpolatedDos() {
        return interpolatedDos;
    }

    public void setCosineDistances(int toMaterialId, double distance) {
        cosineDistances.put(toMaterialId, distance);
    }

    public HashMap<Integer, Double> getCosineDistances() {
        return cosineDistances;
    }

    public void setDistances(int toMaterialId, double distance) {
        distances.put(toMaterialId, distance);
    }

    public HashMap<Integer, Double> getDistances() {
        return distances;
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

    public ArrayList<Double> getInterpolatedEnergy() {
        return interpolatedEnergy;
    }

    public void setInterpolatedEnergy(ArrayList<Double> interpolatedEnergy) {
        this.interpolatedEnergy = interpolatedEnergy;
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

        if (!interpolatedEnergy.isEmpty()) {
            for (Double value : interpolatedEnergy) {
                stringBuilder.append(value);
                stringBuilder.append(" ");
            }
            stringBuilder.append("\n");
        } else {
            stringBuilder.append("No interpolated energy for" +
                    " " + materialId);
            stringBuilder.append("\n");
        }

        if (!interpolatedDos.isEmpty()) {
            for (Double value : interpolatedDos) {
                stringBuilder.append(value);
                stringBuilder.append(" ");
            }

            stringBuilder.append("\n");
            stringBuilder.append(interpolatedDos.size());
            stringBuilder.append(interpolatedEnergy.size());
            stringBuilder.append("\n");
        } else {
            stringBuilder.append("No interpolated energy for" +
                    " " + materialId);
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
