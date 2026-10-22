public class Hank_BankAccount {
    private String name;
    private double money;

    public Hank_BankAccount(String name, double money) {
        this.name = name;
        this.money = money;
    }

    public void addMoney(double value) {
        if (value > 0) {
            money += value;
        }
    }

    public boolean takeMoney(double value) {
        if (value > 0 && value <= money) {
            money -= value;
            return true;
        }
        return false;
    }

    public double getMoney() {
        return money;
    }

    public static void main(String[] args) {
        Hank_BankAccount acc = new Hank_BankAccount("Hank", 200);
        acc.addMoney(25);
        acc.takeMoney(60);
        System.out.println(acc.getMoney());
    }
}
