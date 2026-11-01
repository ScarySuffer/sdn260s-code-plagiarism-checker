// Sorts an array of marks using bubble sort
class Student_A {
    public static void main(String[] args) {
        int[] marks = {64, 52, 91, 38, 77};
        int n = marks.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (marks[j] > marks[j + 1]) {
                    int temp = marks[j];
                    marks[j] = marks[j + 1];
                    marks[j + 1] = temp;
                }
            }
        }
        for (int k = 0; k < n; k++) {
            System.out.println(marks[k]);
        }
    }
}
