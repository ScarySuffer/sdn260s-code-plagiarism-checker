package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResultStoreTest {

    @TempDir
    Path tmp;

    private List<SimilarityResult> sampleResults() {
        return new SimilarityChecker().compareAll(Benchmark.generate(6, 15), new CodeProcessor(), new CodeTokenizer());
    }

    @Test
    void savedResultsLoadBackIdentically() throws IOException {
        List<SimilarityResult> original = sampleResults();
        Path file = tmp.resolve("results.g8");
        ResultStore.save(original, file);
        List<SimilarityResult> loaded = ResultStore.load(file);
        assertEquals(original.size(), loaded.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).getFileA(), loaded.get(i).getFileA());
            assertEquals(original.get(i).getFileB().getContent(), loaded.get(i).getFileB().getContent());
            assertEquals(original.get(i).getSimilarityPercent(), loaded.get(i).getSimilarityPercent());
            assertEquals(original.get(i).isFlagged(), loaded.get(i).isFlagged());
            assertEquals(original.get(i).getMatchingFragments(), loaded.get(i).getMatchingFragments());
        }
    }

    @Test
    void emptyListRoundTrips() throws IOException {
        Path file = tmp.resolve("empty.g8");
        ResultStore.save(new java.util.ArrayList<SimilarityResult>(), file);
        assertEquals(0, ResultStore.load(file).size());
    }

    @Test
    void aFileThatIsNotSerializedDataIsRejected() throws IOException {
        Path file = tmp.resolve("junk.g8");
        Files.write(file, "this is plain text".getBytes("UTF-8"));
        assertThrows(IOException.class, () -> ResultStore.load(file));
    }

    @Test
    void aListOfTheWrongThingsIsRejected() throws IOException {
        Path file = tmp.resolve("strings.g8");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(new java.util.ArrayList<>(java.util.Arrays.asList("a", "b")));
        }
        assertThrows(IOException.class, () -> ResultStore.load(file));
    }

    @Test
    void classesOutsideTheAllowedListAreRejected() throws IOException {
        Path file = tmp.resolve("other.g8");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(new java.io.File("x"));
        }
        assertThrows(IOException.class, () -> ResultStore.load(file));
    }

    @Test
    void missingFileGivesAnIOException() {
        assertThrows(IOException.class, () -> ResultStore.load(tmp.resolve("nope.g8")));
    }
}
