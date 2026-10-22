package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void holdsTwoValuesOfDifferentTypes() {
        Pair<Integer, String> pair = Pair.of(3, "three");
        assertEquals(3, pair.getFirst().intValue());
        assertEquals("three", pair.getSecond());
    }

    @Test
    void equalWhenBothValuesAreEqual() {
        assertEquals(Pair.of(1, "a"), Pair.of(1, "a"));
        assertEquals(Pair.of(1, "a").hashCode(), Pair.of(1, "a").hashCode());
        assertNotEquals(Pair.of(1, "a"), Pair.of(2, "a"));
    }

    @Test
    void toStringShowsBothValues() {
        assertEquals("(1, x)", Pair.of(1, "x").toString());
    }
}
