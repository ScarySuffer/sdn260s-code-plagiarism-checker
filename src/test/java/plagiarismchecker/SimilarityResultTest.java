package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimilarityResultTest {

    private static final CodeFile A = new CodeFile(Path.of("A.java"), "a");
    private static final CodeFile B = new CodeFile(Path.of("B.java"), "b");

    @Test
    void rejectsPercentagesOutsideZeroToHundred() {
        assertThrows(IllegalArgumentException.class, () -> new SimilarityResult(A, B, -1, List.of(), false));
        assertThrows(IllegalArgumentException.class, () -> new SimilarityResult(A, B, 100.1, List.of(), false));
        assertThrows(IllegalArgumentException.class, () -> new SimilarityResult(A, B, Double.NaN, List.of(), false));
    }

    @Test
    void sortsHighestFirst() {
        List<SimilarityResult> results = new ArrayList<>();
        results.add(new SimilarityResult(A, B, 20, List.of(), false));
        results.add(new SimilarityResult(A, B, 90, List.of(), true));
        results.add(new SimilarityResult(A, B, 55, List.of(), false));

        results.sort(SimilarityResult.HIGHEST_FIRST);

        assertEquals(90, results.get(0).getSimilarityPercent());
        assertEquals(20, results.get(2).getSimilarityPercent());
    }

    @Test
    void fragmentListCannotBeChangedFromOutside() {
        List<String> fragments = new ArrayList<>(List.of("x"));
        SimilarityResult r = new SimilarityResult(A, B, 10, fragments, false);
        fragments.add("y");
        assertEquals(1, r.getMatchingFragments().size());
        assertThrows(UnsupportedOperationException.class, () -> r.getMatchingFragments().add("z"));
    }
}
