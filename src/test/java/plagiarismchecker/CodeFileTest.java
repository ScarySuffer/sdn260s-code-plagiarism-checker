package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CodeFileTest {

    @Test
    void filesWithTheSamePathAreEqual() {
        CodeFile a = new CodeFile(Path.of("x", "A.java"), "one");
        CodeFile b = new CodeFile(Path.of("x", "..", "x", "A.java"), "two");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void filesWithDifferentPathsAreNotEqual() {
        assertNotEquals(new CodeFile(Path.of("A.java"), "x"), new CodeFile(Path.of("B.java"), "x"));
    }

    @Test
    void countsLines() {
        assertEquals(3, new CodeFile(Path.of("A.java"), "a\nb\nc").getLineCount());
        assertEquals(1, new CodeFile(Path.of("A.java"), "a\n").getLineCount());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new CodeFile(null, "x"));
        assertThrows(NullPointerException.class, () -> new CodeFile(Path.of("A.java"), null));
    }
}
