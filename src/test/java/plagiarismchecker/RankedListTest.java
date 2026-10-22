package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class RankedListTest {

    @Test
    void keepsItemsInOrderAsTheyAreAdded() {
        RankedList<Integer> list = new RankedList<>(Comparator.reverseOrder());
        for (int n : new int[] {5, 1, 9, 3, 7}) {
            list.add(n);
        }
        assertEquals(Arrays.asList(9, 7, 5, 3, 1), list.toList());
        assertEquals(5, list.size());
    }

    @Test
    void growsPastItsStartingCapacity() {
        RankedList<Integer> list = new RankedList<>(Comparator.naturalOrder());
        for (int i = 100; i > 0; i--) {
            list.add(i);
        }
        assertEquals(100, list.size());
        assertEquals(1, list.get(0).intValue());
        assertEquals(100, list.get(99).intValue());
    }

    @Test
    void equalItemsStayInTheOrderAdded() {
        RankedList<String> list = new RankedList<>(Comparator.comparingInt(String::length));
        list.add("bb");
        list.add("aa");
        list.add("c");
        list.add("cc");
        assertEquals(Arrays.asList("c", "bb", "aa", "cc"), list.toList());
    }

    @Test
    void topReturnsTheFirstItemsOrFewer() {
        RankedList<Integer> list = new RankedList<>(Comparator.naturalOrder());
        list.add(3);
        list.add(1);
        list.add(2);
        assertEquals(Arrays.asList(1, 2), list.top(2));
        assertEquals(3, list.top(10).size());
        assertTrue(list.top(0).isEmpty());
    }

    @Test
    void iteratesInOrder() {
        RankedList<Integer> list = new RankedList<>(Comparator.naturalOrder());
        list.add(2);
        list.add(1);
        int expected = 1;
        for (int n : list) {
            assertEquals(expected++, n);
        }
    }

    @Test
    void rejectsBadInput() {
        RankedList<Integer> list = new RankedList<>(Comparator.naturalOrder());
        assertThrows(IllegalArgumentException.class, () -> list.add(null));
        assertThrows(IllegalArgumentException.class, () -> new RankedList<Integer>(null));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IllegalArgumentException.class, () -> list.top(-1));
    }

    @Test
    void matchesAStableSortOnSimilarityResults() {
        List<SimilarityResult> unsorted = TestResults.results(40);
        RankedList<SimilarityResult> ranked = new RankedList<>(SimilarityResult.HIGHEST_FIRST);
        for (SimilarityResult r : unsorted) {
            ranked.add(r);
        }
        List<SimilarityResult> sorted = new java.util.ArrayList<>(unsorted);
        sorted.sort(SimilarityResult.HIGHEST_FIRST);
        assertEquals(sorted, ranked.toList());
    }
}
