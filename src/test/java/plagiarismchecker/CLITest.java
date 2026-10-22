package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CLITest {

    @TempDir
    Path dir;

    private String runWith(String script) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new CLI(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(bytes, true, StandardCharsets.UTF_8), new Logger(null, false)).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void fullWalkThroughLoadsComparesAndWritesAReport() throws Exception {
        Files.writeString(dir.resolve("A.java"), "class A { int x = 1; }");
        Files.writeString(dir.resolve("B.java"), "class B { int y = 2; }");
        Path report = dir.resolve("report.csv");

        String out = runWith("1\n" + dir + "\n2\n3\n4\n5\n" + report + "\n6\n");

        assertTrue(out.contains("2 file(s) loaded in total."));
        assertTrue(out.contains("Compared 1 pair(s)"));
        assertTrue(out.contains("A.java <-> B.java: 100.0% (flagged for review)"));
        assertTrue(out.contains("Report written to"));
        assertTrue(Files.exists(report));
        assertTrue(out.contains("Goodbye."));
    }

    @Test
    void guidesTheUserWhenThereIsNothingToDo() {
        String out = runWith("2\n3\n4\n5\nx\n6\n");
        assertTrue(out.contains("No submissions loaded yet."));
        assertTrue(out.contains("Load at least two files first."));
        assertTrue(out.contains("No results yet."));
        assertTrue(out.contains("Please choose a number from 1 to 6."));
    }

    @Test
    void stopsCleanlyWhenInputEnds() {
        assertTrue(runWith("").contains("Choose:"));
    }
}
