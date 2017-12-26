import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HumanCannonball {
    static BufferedReader reader;
    public static void main(String[] args)
            throws IOException {
        reader =
                new BufferedReader(
                        new InputStreamReader((
                                System.in)));
        loopSeries();
        reader.close();
    }

    private static int f(int n) {
        return 3*n;
    }

    private static void loopSeries() throws IOException {
        int q = Integer.parseInt(reader.readLine());
        for (int query = 0; query < q; query++) {
            String input = reader.readLine();
            String[] inputs = input.split(" ");
            int a = Integer.parseInt(inputs[0]);
            int b = Integer.parseInt(inputs[1]);
            int n = Integer.parseInt(inputs[2]);
            int sum = 0;
            for (int i = 0; i < n; i++) {
                sum += a;
                for (int j = 0; j < i; j++) {
                    int partial =
                            (int) Math.pow(2.0,
                                    (double) j);
                    partial *= b;
                    sum += partial;
                    System.out.print(sum + " ");
                }
                System.out.println();
            }
        }
    }

    private static void makingAnagrams() throws IOException {
        String firstString = reader.readLine();
        String secondString = reader.readLine();
        int result
                = numberNeededForDeletion
                    (firstString, secondString);
        System.out.println(result);
    }

    private static int numberNeededForDeletion
            (String firstString, String secondString) {
        int[] firstArray = fillArray(firstString);
        int[] secondArray = fillArray(secondString);
        for (int e : firstArray)
            System.err.print(e + " ");
        System.err.println("");
        for (int e : secondArray)
            System.err.print(e + " ");
        System.err.println("");
        int deletions = 0;
        for (int i = 0; i < 26; i++) {
            deletions +=
                    Math.abs(firstArray[i]
                            - secondArray[i]);
        }
        return deletions;
    }

    private static int[] fillArray(String inputString) {
        int[] array = new int[26];
        for (int i = 0; i < inputString.length(); i++) {
            char ch = inputString.charAt(i);
            array[ch - 'a']++;
        }
        return array;
    }

    private static int[]
        performSecondRotation(int[] a, int k) {
        for (int j = 0; j < k; j++) {
            int n = a.length;
            int[] b = new int[n];
            b[n-1] = a[0];
            for (int i = 1; i < n; i++) {
                int c = a[i];
                b[i-1] = c;
            }
            a = b;
        }
        return a;
    }

    /**
     * Wrong answer on both first and second.
     * @throws IOException
     */
    private static void secondLeftRotation() throws IOException {
        String parameters = reader.readLine();
        String[] splitParameters = parameters.split(" ");
        int n = Integer.parseInt(splitParameters[0]);
        int k = Integer.parseInt(splitParameters[1]);
        String inputString = reader.readLine();
        int[] numberArray = new int[n];
        String[] inputArray = inputString.split(" ");
        for (int index = 0; index < n; index++) {
            numberArray[index]
                    = Integer.parseInt(inputArray[index]);
        }
        numberArray = performSecondRotation(numberArray, k);
        StringBuilder stringBuilder = new StringBuilder();
        for (int element : numberArray) {
            stringBuilder.append(element);
            stringBuilder.append(" ");
        }
        String result = stringBuilder.toString();
        String outputString = method(result);
        System.out.println(outputString);
    }

    private static int[]
        performRotation(int[] a, int k) {
        for (int i = 0; i < k; i++) {
            int n = a.length;
            int b = a[0];
            int e = a[1];
            a[0] = e;
            a[n-1] = b;
            for (int q : a)
                System.err.print(q + " ");
            System.err.println("");
        }
        return a;
    }

    private static void leftRotation() throws IOException {
        String parameters = reader.readLine();
        String[] splitParameters = parameters.split(" ");
        int n = Integer.parseInt(splitParameters[0]);
        int k = Integer.parseInt((splitParameters[1]));
        String array = reader.readLine();
        ArrayList<String> permutation = new ArrayList<>();
        for (int charIndex = 0; charIndex <
                array.length(); charIndex += 2) {
            String character
                    = array.charAt(charIndex) + "";
            permutation.add(character);
        }
        permutation = permute(permutation, k);
        StringBuilder stringBuilder = new StringBuilder();
        for (String element : permutation)
            stringBuilder.append(element + " ");
        String result = stringBuilder.toString();
        result = method(result);
        System.out.println(result);
    }

    private static ArrayList<String>
        permute(ArrayList<String> permutation, int k) {
        for (int index = 0; index < k; index++) {
            int firstIndex = 0;
            String character = permutation.get(firstIndex);
            permutation.add(character);
            permutation.remove(firstIndex);
        }
        return permutation;
    }

    public static String method(String str) {
        if (str != null && str.length() > 0
                && str.charAt(str.length() - 1) == ' ') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    private static void deathKnight() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        String losingSequence = "CD";
        int counter = 0;
        for (int battle = 0; battle < N; battle++) {
            String sequence = reader.readLine();
            if (sequence.contains(losingSequence))
                counter++;
        }
        System.out.println(N - counter);
    }

    private static void quickEstimates() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        for (int index = 0; index < N; index++) {
            String cost = reader.readLine();
            System.out.println(cost.length());
        }
    }

    private static void quickBrownFox() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        for (int index = 0; index < N; index++) {
            String input = reader.readLine().toLowerCase();
            String missing = "";
            for (int charIndex = 0; charIndex
                    < alphabet.length(); charIndex++) {
                String character
                        = alphabet.charAt(charIndex) + "";
                if (!input.contains(character)) {
                    missing += character;
                }
            }
            if (missing.length() == 0)
                System.out.println("pangram");
            else
                System.out.println("missing " + missing);
        }
    }

    /**
     * Wrong answer on test case 2/2
     * @throws IOException
     */
    private static void cd() throws IOException {
        String firstLineInput = reader.readLine();
        String[] split = firstLineInput.split(" ");
        int N = Integer.parseInt(split[0]);
        int M = Integer.parseInt(split[1]);
        if (N == 0 || M == 0) {
            System.out.println(0);
            System.exit(0);
        }
        int[] jacks = new int[N];
        for (int index = 0; index < N; index++) {
            int cd = Integer.parseInt(reader.readLine());
            jacks[index] = cd;
        }
        int counter = 0;
        for (int index = 0; index < M; index++) {
            int cd = Integer.parseInt(reader.readLine());
            boolean contains
                    = IntStream.of(jacks).anyMatch(x -> x == cd);
            if (contains) counter++;
        }
        System.out.println(counter);
    }

    private static void filip() throws IOException {
        String input = reader.readLine();
        String[] split = input.split(" ");
        String firstReversed = new StringBuilder(split[0])
                .reverse().toString();
        String secondReversed = new StringBuilder(split[1])
                .reverse().toString();
        int firstNumber = Integer.parseInt(firstReversed);
        int secondNumber = Integer.parseInt(secondReversed);
        if (firstNumber > secondNumber)
            System.out.println(firstNumber);
        else
            System.out.println(secondNumber);
    }

    private static void icpawards() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        String winners = "";
        int numberOfWinners = 12;
        int counter = 0;
        for (int index = 0; index < N; index++) {
            if (counter == numberOfWinners) break;
            String team = reader.readLine();
            String university = team.split(" ")[0];
            if (winners.contains(university))
                continue;
            else {
                winners += team + "\n";
                counter++;
            }
        }
        System.out.println(winners);
    }

    private static void beekeeper() throws IOException {
        String vowels = "aeiouy";
        while (true) {
            int N = Integer.parseInt(reader.readLine());
            HashMap<String, Integer> result = new HashMap<>();
            if (N == 0) break;
            for (int index = 0; index < N; index++) {
                String word = reader.readLine();
                int doubleVowelCounter = 0;
                for (int charIndex = 0; charIndex
                        < word.length() - 1; charIndex++) {
                    String currentCharacter = word.charAt(charIndex) + "";
                    String nextCharacter = word.charAt(charIndex + 1) + "";
                    if (currentCharacter.equals(nextCharacter)) {
                        if (vowels.contains(currentCharacter)
                                && vowels.contains(nextCharacter))
                            doubleVowelCounter++;
                    }
                }
                result.put(word, doubleVowelCounter);
            }
            String resultingString = Collections.max(result.entrySet(),
                    Comparator.comparingInt(Map.Entry::getValue)).getKey();
            System.out.println(resultingString);
        }
    }

    private static void candidate() throws IOException {
        int N = Integer.parseInt(reader.readLine());
        for (int index = 0; index < N; index++) {
            String input = reader.readLine();
            if (input.contains("+")) {
                String plus = "\\+";
                String[] split = input.split(plus);
                int sum = Integer.parseInt(split[0])
                        + Integer.parseInt(split[1]);
                System.out.println(sum);
            } else {
                System.out.println("skipped");
            }
        }
    }

    private static void drmMessages() throws IOException {
        String input = reader.readLine();
        String firstHalf = input.substring(0, input.length()/2);
        String secondHalf = input.substring(input.length()/2);
        System.err.println(firstHalf + " " + secondHalf);
        int sum = 0;
        for (int index = 0; index < firstHalf.length(); index++) {
            char characterAtIndex = firstHalf.charAt(index);
            int asciiValue = (int) characterAtIndex;
            sum += asciiValue - 65;
        }
        System.err.println(sum);
    }

    private static void zamka() throws IOException {
        int L = Integer.parseInt(reader.readLine());
        int D = Integer.parseInt(reader.readLine());
        int X = Integer.parseInt(reader.readLine());
        int minResult = 0;
        int maxResult = 0;
        for (int N = L; N <= D; N++) {
            if (digitSum(N) == X) {
                minResult = N;
                break;
            }
        }
        for (int N = D; N >= L; N--) {
            if (digitSum(N) == X) {
                maxResult = N;
                break;
            }
        }
        System.out.println(minResult);
        System.out.println(maxResult);
    }

    private static int digitSum(int n) {
        int num = n;
        int sum = 0;
        while (num > 0) {
            sum = sum + num % 10;
            num = num / 10;
        }
        return sum;
    }

    private static void tarifa() throws IOException {
        int X = Integer.parseInt(reader.readLine());
        int N = Integer.parseInt(reader.readLine());
        int totalData = X * (N+1);
        int spentData = 0;
        for (int p = 0; p < N; p++) {
            spentData +=
                    Integer.parseInt(
                            reader.readLine());
        }
        System.out.println(totalData - spentData);
    }

    private static void hissingMicrophone() throws IOException {
        String input = reader.readLine();
        Pattern p = Pattern.compile("ss");
        int occurences = 0;
        Matcher m = p.matcher(input); //etc
        while(m.find())
            occurences++;
        if (occurences >= 1) {
            System.out.println("hiss");
        } else
            System.out.println("no hiss");
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
