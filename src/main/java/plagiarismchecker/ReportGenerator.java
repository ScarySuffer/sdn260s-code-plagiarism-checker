    package plagiarismchecker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Produces a readable report (TXT or CSV) of the comparison results.
 *
 * <p>The file type is chosen from the file name: {@code .csv} gives CSV, anything else
 * gives plain text. Numbers always use a decimal point, whatever the computer's language.</p>
 */
public class ReportGenerator {

    static final String DISCLAIMER =
            "A flag only marks a pair for a person to review. It is not a finding of plagiarism.";
    private static final int MAX_FRAGMENTS_IN_TEXT = 10;

    /** Writes the results to the given file (UTF-8), creating folders if needed. */
    public void writeReport(List<SimilarityResult> results, Path outputFile) throws IOException {
        if (results == null || outputFile == null) {
            throw new IllegalArgumentException("results and outputFile must not be null");
        }
        boolean csv = outputFile.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv");
        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(outputFile, (csv ? toCsv(results) : toText(results)).getBytes(StandardCharsets.UTF_8));
    }

    /** @return the report as plain text */
    public String toText(List<SimilarityResult> results) {
        StringBuilder sb = new StringBuilder();
        long flagged = results.stream().filter(SimilarityResult::isFlagged).count();
        sb.append("CODE PLAGIARISM CHECKER - REPORT\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append('\n');
        sb.append(DISCLAIMER).append('\n');
        sb.append("Pairs compared: ").append(results.size()).append(", flagged for review: ").append(flagged)
                .append("\n\n");
        int number = 1;
        for (SimilarityResult r : results) {
            sb.append(number++).append(". ").append(r).append('\n');
            List<String> fragments = r.getMatchingFragments();
            int shown = Math.min(fragments.size(), MAX_FRAGMENTS_IN_TEXT);
            for (int i = 0; i < shown; i++) {
                sb.append("     matching tokens: ").append(fragments.get(i)).append('\n');
            }
            if (fragments.size() > shown) {
                sb.append("     ... and ").append(fragments.size() - shown).append(" more\n");
            }
        }
        sb.append("\nTokens are normalised: ID = any name, NUM = a number, STR = text in quotes.\n");
        return sb.toString();
    }

    /** @return the report as CSV with a header row */
    public String toCsv(List<SimilarityResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("File A,File B,Similarity (%),Flagged for review,Matching fragments\n");
        for (SimilarityResult r : results) {
            sb.append(csv(r.getFileA().getName())).append(',')
                    .append(csv(r.getFileB().getName())).append(',')
                    .append(String.format(Locale.ROOT, "%.1f", r.getSimilarityPercent())).append(',')
                    .append(r.isFlagged() ? "Yes" : "No").append(',')
                    .append(csv(String.join(" | ", r.getMatchingFragments()))).append('\n');
        }
        return sb.toString();
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
