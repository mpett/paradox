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
        for (int id : mId) System.err.println(id);
        System.err.println(mId.size());
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
