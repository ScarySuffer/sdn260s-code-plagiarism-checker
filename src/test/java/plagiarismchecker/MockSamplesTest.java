package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Evaluation on the MOCK submissions in samples/mock (invented, not real students).
 * It records how the PROPOSED method behaves on pairs whose relationship we built on
 * purpose. If the method or threshold changes, update the expectations deliberately.
 */
class MockSamplesTest {

    private List<SimilarityResult> results;

    @BeforeEach
    void analyseMockFolder() {
        ComparisonService service = new ComparisonService(new Logger(null, false));
        results = service.compare(service.load(List.of(Path.of("samples", "mock"))));
    }

    private SimilarityResult pair(String a, String b) {
        for (SimilarityResult r : results) {
            String x = r.getFileA().getName();
            String y = r.getFileB().getName();
            if ((x.equals(a) && y.equals(b)) || (x.equals(b) && y.equals(a))) {
                return r;
            }
        }
        throw new AssertionError("pair not found: " + a + " / " + b);
    }

    @Test
    void comparesEveryPairOfTheElevenFiles() {
        assertEquals(55, results.size());
    }

    @Test
    void flagsExactlyTheThreeBuiltInCopies() {
        Set<String> flagged = new HashSet<>();
        for (SimilarityResult r : results) {
            if (r.isFlagged()) {
                flagged.add(r.getFileA().getName() + "+" + r.getFileB().getName());
            }
        }
        assertEquals(3, flagged.size(), "flagged: " + flagged);
        assertTrue(pair("Alice_Factorial.java", "Bob_Factorial.java").isFlagged());
        assertTrue(pair("Gina_BankAccount.java", "Hank_BankAccount.java").isFlagged());
        assertTrue(pair("Dan_Palindrome.java", "Eve_Palindrome.java").isFlagged());
    }

    @Test
    void doesNotFlagIndependentSolutionsOrUnrelatedFiles() {
        assertFalse(pair("Alice_Factorial.java", "Carol_Factorial.java").isFlagged());
        assertFalse(pair("Dan_Palindrome.java", "Frank_Palindrome.java").isFlagged());
        assertFalse(pair("Ivy_Grades.java", "Jake_FizzBuzz.java").isFlagged());
    }

    @Test
    void partialCopyScoresAboveIndependentWorkButBelowTheFlag() {
        double partial = pair("Gina_BankAccount.java", "Kim_Library.java").getSimilarityPercent();
        double independent = pair("Dan_Palindrome.java", "Frank_Palindrome.java").getSimilarityPercent();
        assertTrue(partial > independent, partial + " vs " + independent);
        assertTrue(partial < SimilarityChecker.DEFAULT_THRESHOLD_PERCENT);
    }

    @Test
    void mostSimilarPairIsFirst() {
        assertTrue(results.get(0).getSimilarityPercent() >= results.get(results.size() - 1).getSimilarityPercent());
        assertEquals(100.0, results.get(0).getSimilarityPercent(), 0.0001);
    }
}
