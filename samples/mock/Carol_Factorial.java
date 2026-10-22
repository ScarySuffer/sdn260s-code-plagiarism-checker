// Carol - factorial using recursion (her own solution)
public class Carol_Factorial {

    static long factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 5; i++) {
            System.out.println(i + "! = " + factorial(i));
        }
    }
}
