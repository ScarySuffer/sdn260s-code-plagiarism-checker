package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path dir;

    private String output;

    private int run(String... args) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int code = Main.run(args, new PrintStream(bytes, true, StandardCharsets.UTF_8), new Logger(null, false));
        output = bytes.toString(StandardCharsets.UTF_8);
        return code;
    }

    @Test
    void noArgumentsShowsUsage() {
        assertEquals(Main.EXIT_USAGE, run());
        assertTrue(output.contains("Usage"));
    }

    @Test
    void loadsAFolderOfSubmissions() throws Exception {
        Files.writeString(dir.resolve("A.java"), "class A {}");
        Files.writeString(dir.resolve("B.java"), "class B {}");

        assertEquals(Main.EXIT_OK, run(dir.toString()));
        assertTrue(output.contains("Loaded 2 file(s)"));
    }

    @Test
    void countsADuplicatePathOnlyOnce() throws Exception {
        Path file = dir.resolve("A.java");
        Files.writeString(file, "class A {}");

        assertEquals(Main.EXIT_OK, run(file.toString(), file.toString()));
        assertTrue(output.contains("Loaded 1 file(s)"));
    }

    @Test
    void reportsWhenNothingCouldBeLoaded() {
        assertEquals(Main.EXIT_NO_FILES, run(dir.resolve("missing.java").toString()));
        assertTrue(output.contains("No usable source files"));
    }

    @Test
    void printsASimilarityTableForTwoOrMoreFiles() throws Exception {
        Files.writeString(dir.resolve("A.java"), "class A { int x = 1; }");
        Files.writeString(dir.resolve("B.java"), "class B { int y = 2; }");

        assertEquals(Main.EXIT_OK, run(dir.toString()));
        assertTrue(output.contains("A.java <-> B.java: 100.0% (flagged for review)"));
        assertTrue(output.contains("not a finding of plagiarism"));
    }

    @Test
    void asksForASecondFileWhenOnlyOneIsLoaded() throws Exception {
        Files.writeString(dir.resolve("A.java"), "class A {}");

        assertEquals(Main.EXIT_OK, run(dir.toString()));
        assertTrue(output.contains("at least two files"));
    }
}
