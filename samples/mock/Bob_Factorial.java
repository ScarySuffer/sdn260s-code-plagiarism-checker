/* Bob's factorial program */
public class Bob_Factorial {
    static long fact(int num) {
        long answer=1;
        for(int k=2;k<=num;k++){
            answer=answer*k;   // multiply
        }
        return answer;
    }
    public static void main(String[] args) {
        int value=7;
        System.out.println("Factorial of "+value+" is "+fact(value));
    }
}
