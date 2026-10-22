package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParallelComparisonTest {

    private final CodeProcessor processor = new CodeProcessor();
    private final CodeTokenizer tokenizer = new CodeTokenizer();
    private final SimilarityChecker checker = new SimilarityChecker();

    private static void assertSameResults(List<SimilarityResult> expected, List<SimilarityResult> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            SimilarityResult e = expected.get(i);
            SimilarityResult a = actual.get(i);
            assertEquals(e.getFileA(), a.getFileA());
            assertEquals(e.getFileB(), a.getFileB());
            assertEquals(e.getSimilarityPercent(), a.getSimilarityPercent());
            assertEquals(e.isFlagged(), a.isFlagged());
            assertEquals(e.getMatchingFragments(), a.getMatchingFragments());
        }
    }

    @Test
    void parallelResultsMatchSingleThreadedResultsExactly() {
        List<CodeFile> files = Benchmark.generate(25, 40);
        List<SimilarityResult> single = checker.compareAll(files, processor, tokenizer);
        for (int threads : new int[] {2, 3, 8}) {
            assertSameResults(single, checker.compareAll(files, processor, tokenizer, threads));
        }
    }

    @Test
    void oneThreadUsesTheSingleThreadedPath() {
        List<CodeFile> files = Benchmark.generate(6, 20);
        assertSameResults(checker.compareAll(files, processor, tokenizer),
                checker.compareAll(files, processor, tokenizer, 1));
    }

    @Test
    void handlesFewerThanTwoFiles() {
        assertTrue(checker.compareAll(Benchmark.generate(1, 5), processor, tokenizer, 4).isEmpty());
        assertTrue(checker.compareAll(Benchmark.generate(0, 5), processor, tokenizer, 4).isEmpty());
    }

    @Test
    void serviceReportsAndValidatesItsThreadCount() {
        Logger logger = new Logger(null, false);
        assertEquals(3, new ComparisonService(logger, checker, 3).getThreads());
        assertThrows(IllegalArgumentException.class, () -> new ComparisonService(logger, checker, 0));
        List<SimilarityResult> results = new ComparisonService(logger, checker, 4).compare(Benchmark.generate(5, 10));
        assertEquals(10, results.size());
    }
}
