// Dan - checks if a word is a palindrome with two pointers
public class Dan_Palindrome {

    static boolean isPalindrome(String text) {
        int left = 0;
        int right = text.length() - 1;
        while (left < right) {
            if (text.charAt(left) != text.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        String word = "racecar";
        System.out.println(word + " palindrome? " + isPalindrome(word));
    }
}
