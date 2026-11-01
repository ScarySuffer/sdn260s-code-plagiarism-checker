// Sorting scores (bubble sort)
class Student_B {
    public static void main(String[] args) {
        int[] scores = {64, 52, 91, 38, 77};
        int len = scores.length;
        for (int x = 0; x < len - 1; x++) {
            for (int y = 0; y < len - x - 1; y++) {
                if (scores[y] > scores[y + 1]) {
                    int holder = scores[y];
                    scores[y] = scores[y + 1];
                    scores[y + 1] = holder;
                }
            }
        }
        System.out.println("Sorted scores:");
        for (int z = 0; z < len; z++) {
            System.out.println(scores[z]);
        }
    }
}
