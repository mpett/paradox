import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HumanCannonball {
    static BufferedReader reader;
    public static void main(String[] args)
            throws IOException {
        reader =
                new BufferedReader(
                        new InputStreamReader((
                                System.in)));
        noDuplicates();
        reader.close();
    }

    private static int f(int n) {
        return 3*n;
    }

    private static void noDuplicates() throws IOException {
        String inputLine = reader.readLine();
        String[] splitInput
                = inputLine.split(" ");
        String result = "yes";
        for (int i = 0; i < splitInput.length; i++) {
            String wordToBeChecked = splitInput[i];
            int matches = 0;
            for (int j = 0; j < splitInput.length; j++) {
                if (wordToBeChecked
                        .equals(splitInput[j])) {
                    matches++;
                }
            }
            if (matches > 1) {
                result = "no";
                break;
            }
        }
        System.out.println(result);
    }

    private static void triTiling() throws IOException {
        boolean thereAreMoreTestCases = true;
        while (thereAreMoreTestCases) {
            int n = Integer.parseInt(reader.readLine());
            int sum = 0;
            while (n > 0) {
                n -= 2;
                sum += f(n);
            }
            System.out.println(2*sum);
        }
    }

    private static void modulo()
                throws IOException {
        int N = 10;
        int modCheck = 42;
        int[] array = new int[N];
        for (int index = 0; index < N; index++) {
            array[index] =
                    Integer.parseInt(
                            reader.readLine())
                            % modCheck;
        }
        System.out.println(
                checkDuplicate(array));
    }

    private static int checkDuplicate(int array[]) {
        int numberOfDistinctValues = 0;
        for (int i = 0; i < array.length; i++) {
            boolean found = false;
            for (int j = 0; j < i; j++)
                if (array[i] == array[j]) {
                    found = true;
                    break;
                }
            if (!found)
                numberOfDistinctValues++;
        }
        return numberOfDistinctValues;
    }

    private static void cannonball() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        for (int index = 0; index < N; index++) {
            String[] input =
                    reader.readLine().split(" ");
            double v0 = Double.parseDouble(input[0]);
            double theta = Double.parseDouble(input[1]);
            double x1 = Double.parseDouble(input[2]);
            double h1 = Double.parseDouble(input[3]);
            double h2 = Double.parseDouble(input[4]);
            double g = 9.81;
            double r = Math.toRadians(theta);
            double t = x1 / (v0 * Math.cos(r));
            double y1 = v0 * t * Math.sin(r)
                    - 0.5 * (g * t * t);
            if (((h1 + 1) <= y1) && (y1 <= (h2 - 1)))
                System.out.println("Safe");
            else
                System.out.println("Not Safe");
        }
    }
}

class Fibonacci
{
    /* function that returns nth Fibonacci number */
    static int fib(int n)
    {
        int F[][] = new int[][]{{1,1},{1,0}};
        if (n == 0)
            return 0;
        power(F, n-1);

        return F[0][0];
    }

    static void multiply(int F[][], int M[][])
    {
        int x =  F[0][0]*M[0][0] + F[0][1]*M[1][0];
        int y =  F[0][0]*M[0][1] + F[0][1]*M[1][1];
        int z =  F[1][0]*M[0][0] + F[1][1]*M[1][0];
        int w =  F[1][0]*M[0][1] + F[1][1]*M[1][1];

        F[0][0] = x;
        F[0][1] = y;
        F[1][0] = z;
        F[1][1] = w;
    }

    /* Optimized version of power() in method 4 */
    static void power(int F[][], int n)
    {
        if( n == 0 || n == 1)
            return;
        int M[][] = new int[][]{{1,1},{1,0}};

        power(F, n/2);
        multiply(F, F);

        if (n%2 != 0)
            multiply(F, M);
    }

    /* Driver program to test above function */
    public static void main (String args[])
    {
        int n = 9;
        System.out.println(fib(n));
    }
}
