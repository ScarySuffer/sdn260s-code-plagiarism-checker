// Simple bank account with deposit and withdraw
class Student_D {
    private double balance;

    Student_D(double opening) {
        balance = opening;
    }

    void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    double getBalance() {
        return balance;
    }

    public static void main(String[] args) {
        Student_D account = new Student_D(100.0);
        account.deposit(50.0);
        boolean ok = account.withdraw(500.0);
        System.out.println("Withdrawal ok? " + ok);
        System.out.println("Balance: " + account.getBalance());
    }
}
