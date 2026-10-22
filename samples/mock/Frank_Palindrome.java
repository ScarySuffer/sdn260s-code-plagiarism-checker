// Frank - palindrome by reversing the text (his own solution)
public class Frank_Palindrome {

    public static void main(String[] args) {
        String original = "madam";
        StringBuilder builder = new StringBuilder(original);
        String reversed = builder.reverse().toString();

        if (original.equals(reversed)) {
            System.out.println("Yes, it reads the same both ways.");
        } else {
            System.out.println("No.");
        }
    }
}
