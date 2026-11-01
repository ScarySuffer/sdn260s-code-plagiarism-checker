// Checks whether a word reads the same forwards and backwards
class Student_B {
    static boolean isPalindrome(String word) {
        int left = 0;
        int right = word.length() - 1;
        while (left < right) {
            if (word.charAt(left) != word.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        String[] words = {"level", "java", "radar"};
        for (String w : words) {
            System.out.println(w + " -> " + isPalindrome(w));
        }
    }
}
