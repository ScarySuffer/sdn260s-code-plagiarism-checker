package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ComparisonServiceTest {

    @TempDir
    Path dir;

    private final Logger logger = new Logger(null, false);
    private final ComparisonService service = new ComparisonService(logger);

    @Test
    void loadsFoldersAndFilesWithoutDuplicates() throws Exception {
        Path a = dir.resolve("A.java");
        Files.writeString(a, "class A {}");
        Files.writeString(dir.resolve("B.java"), "class B {}");

        List<CodeFile> files = service.load(Arrays.asList(dir, a));
        assertEquals(2, files.size());
    }

    @Test
    void skipsBadPathsAndLogsThem() {
        List<CodeFile> files = service.load(Arrays.asList(dir.resolve("missing.java")));
        assertTrue(files.isEmpty());
        assertEquals(1, logger.count(Logger.Level.ERROR));
    }

    @Test
    void comparesAllPairs() throws Exception {
        Files.writeString(dir.resolve("A.java"), "class A { int x = 1; }");
        Files.writeString(dir.resolve("B.java"), "class B { int y = 2; }");
        Files.writeString(dir.resolve("C.java"), "class C { void f() { while (true) { g(); } } }");

        List<SimilarityResult> results = service.compare(service.load(Arrays.asList(dir)));
        assertEquals(3, results.size());
        assertEquals(100.0, results.get(0).getSimilarityPercent(), 0.0001);
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> new ComparisonService(null));
    }
}
