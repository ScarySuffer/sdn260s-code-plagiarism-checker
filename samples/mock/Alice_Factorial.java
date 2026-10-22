// Alice - calculates the factorial of a number using a loop
public class Alice_Factorial {

    static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result = result * i;
        }
        return result;
    }

    public static void main(String[] args) {
        int number = 10;
        System.out.println("Factorial of " + number + " is " + factorial(number));
    }
}
