// Kim - library with a small borrowing system; the money methods are borrowed from Gina
import java.util.ArrayList;
import java.util.List;

public class Kim_Library {
    private List<String> books = new ArrayList<>();
    private double fines;

    public void addBook(String title) {
        books.add(title);
    }

    public boolean borrow(String title) {
        return books.remove(title);
    }

    public void deposit(double amount) {
        if (amount > 0) {
            fines += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= fines) {
            fines -= amount;
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Kim_Library library = new Kim_Library();
        library.addBook("Java Basics");
        System.out.println(library.borrow("Java Basics"));
    }
}
