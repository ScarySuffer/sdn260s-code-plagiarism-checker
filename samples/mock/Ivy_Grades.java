// Ivy - average and highest of some test marks
public class Ivy_Grades {

    public static void main(String[] args) {
        int[] marks = {62, 75, 48, 91, 83};
        int highest = marks[0];
        double total = 0;

        for (int mark : marks) {
            total += mark;
            if (mark > highest) {
                highest = mark;
            }
        }

        System.out.println("Average: " + total / marks.length);
        System.out.println("Highest: " + highest);
    }
}
