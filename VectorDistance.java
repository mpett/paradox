import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class VectorDistance {
    private static final String FILE_PATH =
            "/Users/martinpettersson/diraclaravel" +
                    "/data/materials/_cod_database_code" +
                    "/2213366/DOS/dos.json";
    private static final String MATERIAL_IDS
            = "/Users/martinpettersson" +
            "/diraclaravel/data/materials" +
            "/_cod_database_code/materials.txt";

    public static void main(String[] args) throws IOException {
        ArrayList<Integer> mId = materialIndices(MATERIAL_IDS);
        ArrayList<Double> dosVector = new ArrayList<>();
        ArrayList<Double> energyVector = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
        try {
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
        }
        finally {
            br.close();
        }
        for (double value : dosVector) {
            System.err.println(value);
        }
        System.err.println("");
        for (double value : energyVector) {
            System.err.println(value);
        }
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
}
