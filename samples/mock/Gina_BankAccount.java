// Gina - simple bank account
public class Gina_BankAccount {
    private String owner;
    private double balance;

    public Gina_BankAccount(String owner, double balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public double getBalance() {
        return balance;
    }

    public static void main(String[] args) {
        Gina_BankAccount account = new Gina_BankAccount("Gina", 100);
        account.deposit(50);
        account.withdraw(30);
        System.out.println(account.getBalance());
    }
}
