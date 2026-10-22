// Eve - palindrome checker
public class Eve_Palindrome {

    static boolean check(String s) {
        int unused = 0;
        int j = s.length() - 1;
        int i = 0;
        while (i < j) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
            j--;
            i++;
        }
        return true;
    }

    public static void main(String[] args) {
        String input = "level";
        boolean answer = check(input);
        System.out.println(input + " palindrome? " + answer);
    }
}
