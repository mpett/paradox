import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Sibice {
    public static void main(String[] args)
            throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));

        String[] firstInput =
                reader.readLine()
                        .split(" ");

        int N = Integer
                .parseInt(firstInput[0]);
        int W = Integer
                .parseInt(firstInput[1]);
        int H = Integer
                .parseInt(firstInput[2]);

        int maximumMatchLength
                = (int) Math.sqrt(
                        Math.pow((double) W, 2)
                            + Math.pow((double) H, 2));

        for (int index = 0; index < N; index++) {
            int match = Integer
                    .parseInt(reader.readLine());
            String result =
                    match <= maximumMatchLength
                            ? "DA" : "NE";
            System.out.println(result);
        }
    }
}
