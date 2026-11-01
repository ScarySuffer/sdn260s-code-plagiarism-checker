// Calculates the average of daily temperatures
class Student_A {
    public static void main(String[] args) {
        double[] temps = {21.5, 23.0, 19.8, 25.2, 22.1};
        double total = 0;
        for (double t : temps) {
            total += t;
        }
        double average = total / temps.length;
        System.out.println("Average temperature: " + average);
    }
}
