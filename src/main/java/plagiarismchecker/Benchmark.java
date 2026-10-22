package plagiarismchecker;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Measures how long a comparison takes as the number of files grows, on one thread and on
 * several. Run it with {@code java -cp target/classes plagiarismchecker.Benchmark}.
 *
 * <p>The files are generated here (a fixed random seed makes every run use the same
 * files), so no student work is needed. Times depend on the computer, so they are only
 * meaningful when compared on the same machine.</p>
 */
public final class Benchmark {

    private Benchmark() {
    }

    public static void main(String[] args) {
        int threads = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
        int[] sizes = {10, 20, 40, 80, 160};
        CodeProcessor processor = new CodeProcessor();
        CodeTokenizer tokenizer = new CodeTokenizer();
        SimilarityChecker checker = new SimilarityChecker();

        System.out.println("Comparison time (milliseconds), median of 5 runs after 2 warm-up runs");
        System.out.println("Worker threads for the parallel column: " + threads
                + "  (this computer reports " + Runtime.getRuntime().availableProcessors() + " processor(s))");
        System.out.println();
        System.out.printf(Locale.ROOT, "%6s %8s %14s %14s %9s%n", "files", "pairs", "1 thread", threads + " threads", "speed-up");
        for (int n : sizes) {
            List<CodeFile> files = generate(n, 120);
            double single = median(() -> checker.compareAll(files, processor, tokenizer, 1));
            double parallel = median(() -> checker.compareAll(files, processor, tokenizer, threads));
            System.out.printf(Locale.ROOT, "%6d %8d %14.1f %14.1f %8.2fx%n", n, n * (n - 1) / 2, single, parallel,
                    single / parallel);
        }
    }

    private static double median(Runnable job) {
        for (int i = 0; i < 2; i++) {
            job.run();
        }
        double[] times = new double[5];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            job.run();
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
        }
        java.util.Arrays.sort(times);
        return times[times.length / 2];
    }

    /** Builds {@code count} synthetic files of about {@code statements} statements each. */
    static List<CodeFile> generate(int count, int statements) {
        Random random = new Random(42);
        List<CodeFile> files = new ArrayList<>();
        for (int f = 0; f < count; f++) {
            StringBuilder sb = new StringBuilder("public class Gen" + f + " {\n");
            for (int s = 0; s < statements; s++) {
                switch (random.nextInt(4)) {
                    case 0:
                        sb.append("  int v").append(s).append(" = ").append(random.nextInt(100)).append(" + ")
                                .append(random.nextInt(100)).append(";\n");
                        break;
                    case 1:
                        sb.append("  for (int i = 0; i < ").append(random.nextInt(50)).append("; i++) { total += i; }\n");
                        break;
                    case 2:
                        sb.append("  if (x").append(random.nextInt(10)).append(" > ").append(random.nextInt(9))
                                .append(") { System.out.println(\"a").append(s).append("\"); }\n");
                        break;
                    default:
                        sb.append("  String s").append(s).append(" = name.substring(").append(random.nextInt(5))
                                .append(").trim();\n");
                        break;
                }
            }
            sb.append("}\n");
            files.add(new CodeFile(Paths.get("Gen" + f + ".java"), sb.toString()));
        }
        return files;
    }
}
