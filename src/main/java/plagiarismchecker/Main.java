package plagiarismchecker;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Starts the application and connects the main components.
 *
 * <p>At this stage it loads the files or folders given as arguments, lists them and, when
 * there are two or more, prints a similarity table (method approved by the group, see
 * {@link SimilarityChecker}). With {@code --gui} it opens the Swing window and with {@code --cli} the text menu.</p>
 */
public class Main {

    static final int EXIT_OK = 0;
    static final int EXIT_NO_FILES = 1;
    static final int EXIT_USAGE = 2;

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--gui")) {
            new GUI().show();
            return;
        }
        if (args.length == 1 && args[0].equals("--cli")) {
            new CLI(System.in, System.out, new Logger(Path.of("logs", "plagiarism-checker.log"), false)).run();
            return;
        }
        Logger logger = new Logger(Path.of("logs", "plagiarism-checker.log"), true);
        System.exit(run(args, System.out, logger));
    }

    /**
     * Loads the given files and folders and prints a summary.
     *
     * @return {@link #EXIT_OK}, {@link #EXIT_NO_FILES} if nothing could be loaded, or
     *         {@link #EXIT_USAGE} if no arguments were given
     */
    static int run(String[] args, PrintStream out, Logger logger) {
        out.println("========================================");
        out.println("       CODE PLAGIARISM CHECKER");
        out.println("========================================");

        if (args.length == 0) {
            out.println("Usage: java -jar code-plagiarism-checker.jar <file.java | folder> [more ...]");
            out.println("       java -jar code-plagiarism-checker.jar --gui   (open the window)");
            out.println("       java -jar code-plagiarism-checker.jar --cli   (text menu)");
            return EXIT_USAGE;
        }

        ComparisonService service = new ComparisonService(logger);
        List<Path> paths = new ArrayList<>();
        for (String arg : args) {
            paths.add(Path.of(arg));
        }
        List<CodeFile> files = service.load(paths);
        if (files.isEmpty()) {
            out.println("No usable source files were loaded. See the messages above.");
            return EXIT_NO_FILES;
        }

        out.println("Loaded " + files.size() + " file(s):");
        for (int i = 0; i < files.size(); i++) {
            out.println("  " + (i + 1) + ". " + files.get(i));
        }

        if (files.size() < 2) {
            out.println("Add at least two files to compare them.");
            return EXIT_OK;
        }

        SimilarityChecker checker = service.getChecker();
        List<SimilarityResult> results = service.compare(files);
        out.println();
        out.printf(Locale.ROOT, "Similarity (method: %d-token groups, flag at %.0f%%)%n",
                checker.getGramSize(), checker.getThresholdPercent());
        for (SimilarityResult result : results) {
            out.println("  " + result);
        }
        out.println(ReportGenerator.DISCLAIMER);
        return EXIT_OK;
    }
}
