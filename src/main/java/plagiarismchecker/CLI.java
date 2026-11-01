package plagiarismchecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

/**
 * The command-line menu: add submissions, view submissions, compare, view results,
 * generate a report, exit.
 *
 * <p>It uses the same {@link ComparisonService} as the Swing {@link GUI}.</p>
 */
public class CLI {

    private final Scanner in;
    private final PrintStream out;
    private final ComparisonService service;
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private final List<CodeFile> files = new ArrayList<>();
    private List<SimilarityResult> results = new ArrayList<>();

    public CLI(InputStream input, PrintStream out, Logger logger) {
        this.in = new Scanner(input);
        this.out = out;
        this.service = new ComparisonService(logger);
    }

    /** Runs the menu until the user chooses to exit (or the input ends). */
    public void run() {
        while (true) {
            printMenu();
            String choice = readLine();
            if (choice == null) {
                return;
            }
            switch (choice.trim()) {
                case "1":
                    addSubmissions();
                    break;
                case "2":
                    viewSubmissions();
                    break;
                case "3":
                    compare();
                    break;
                case "4":
                    viewResults();
                    break;
                case "5":
                    generateReport();
                    break;
                case "6":
                    out.println("Goodbye.");
                    return;
                default:
                    out.println("Please choose a number from 1 to 6.");
            }
        }
    }

    private void printMenu() {
        out.println();
        out.println("--- Code Plagiarism Checker ---");
        out.println("1. Add submissions (file or folder)");
        out.println("2. View submissions");
        out.println("3. Compare");
        out.println("4. View results");
        out.println("5. Generate report");
        out.println("6. Exit");
        out.print("Choose: ");
        out.flush();
    }

    private void addSubmissions() {
        out.print("Path to a .java file or a folder: ");
        out.flush();
        String line = readLine();
        if (line == null || line.trim().isEmpty()) {
            out.println("No path entered.");
            return;
        }
        Set<CodeFile> merged = new LinkedHashSet<>(files);
        merged.addAll(service.load(List.of(Path.of(line.trim()))));
        files.clear();
        files.addAll(merged);
        out.println(files.size() + " file(s) loaded in total.");
    }

    private void viewSubmissions() {
        if (files.isEmpty()) {
            out.println("No submissions loaded yet.");
            return;
        }
        for (int i = 0; i < files.size(); i++) {
            out.println("  " + (i + 1) + ". " + files.get(i));
        }
    }

    private void compare() {
        if (files.size() < 2) {
            out.println("Load at least two files first.");
            return;
        }
        results = service.compare(files);
        out.println("Compared " + results.size() + " pair(s). Choose 4 to view them.");
    }

    private void viewResults() {
        if (results.isEmpty()) {
            out.println("No results yet. Run Compare first.");
            return;
        }
        out.printf(Locale.ROOT, "Similarity (method: %d-token groups, flag at %.0f%%)%n",
                service.getChecker().getGramSize(), service.getChecker().getThresholdPercent());
        for (SimilarityResult r : results) {
            out.println("  " + r);
        }
        out.println(ReportGenerator.DISCLAIMER);
    }

    private void generateReport() {
        if (results.isEmpty()) {
            out.println("No results yet. Run Compare first.");
            return;
        }
        out.print("Report file name (.txt or .csv) [plagiarism-report.txt]: ");
        out.flush();
        String name = readLine();
        if (name == null || name.trim().isEmpty()) {
            name = "plagiarism-report.txt";
        }
        try {
            reportGenerator.writeReport(results, Path.of(name.trim()));
            out.println("Report written to " + name.trim());
        } catch (IOException e) {
            out.println("Could not write the report: " + e.getMessage());
        }
    }

    private String readLine() {
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
