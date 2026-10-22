package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportGeneratorTest {

    @TempDir
    Path dir;

    private final ReportGenerator generator = new ReportGenerator();

    private static List<SimilarityResult> sample() {
        CodeFile a = new CodeFile(Path.of("A.java"), "x");
        CodeFile b = new CodeFile(Path.of("B,odd.java"), "y");
        return Arrays.asList(
                new SimilarityResult(a, b, 87.5, Arrays.asList("say \"hi\" ;"), true),
                new SimilarityResult(b, a, 10.0, Collections.<String>emptyList(), false));
    }

    @Test
    void textReportListsPairsAndTheDisclaimer() {
        String text = generator.toText(sample());
        assertTrue(text.contains("Pairs compared: 2, flagged for review: 1"));
        assertTrue(text.contains("87.5% (flagged for review)"));
        assertTrue(text.contains("matching tokens: say \"hi\" ;"));
        assertTrue(text.contains("not a finding of plagiarism"));
    }

    @Test
    void csvHasHeaderAndEscapesQuotesAndUsesDecimalPoint() {
        String[] lines = generator.toCsv(sample()).split("\n");
        assertEquals("File A,File B,Similarity (%),Flagged for review,Matching fragments", lines[0]);
        assertEquals("\"A.java\",\"B,odd.java\",87.5,Yes,\"say \"\"hi\"\" ;\"", lines[1]);
        assertEquals("\"B,odd.java\",\"A.java\",10.0,No,\"\"", lines[2]);
    }

    @Test
    void writesCsvOrTextByExtensionAndCreatesFolders() throws Exception {
        Path csv = dir.resolve("out").resolve("r.csv");
        generator.writeReport(sample(), csv);
        assertTrue(Files.readString(csv, StandardCharsets.UTF_8).startsWith("File A,"));

        Path txt = dir.resolve("r.txt");
        generator.writeReport(sample(), txt);
        assertTrue(Files.readString(txt, StandardCharsets.UTF_8).startsWith("CODE PLAGIARISM CHECKER"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(IllegalArgumentException.class, () -> generator.writeReport(null, dir.resolve("x.txt")));
        assertThrows(IllegalArgumentException.class, () -> generator.writeReport(sample(), null));
    }
}
