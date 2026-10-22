import java.util.Scanner;

public class StudentC {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Name: ");
        String name = in.nextLine();
        if (name.length() > 2) {
            System.out.println("Hello " + name);
        }
    }
}
