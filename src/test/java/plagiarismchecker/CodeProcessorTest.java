package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CodeProcessorTest {

    private final CodeProcessor processor = new CodeProcessor();

    @Test
    void removesLineAndBlockComments() {
        String out = processor.normalise("int a = 1; // note\n/* big\ncomment */ int b = 2;");
        assertFalse(out.contains("note"));
        assertFalse(out.contains("comment"));
        assertTrue(out.contains("int a = 1;"));
        assertTrue(out.contains("int b = 2;"));
    }

    @Test
    void keepsCommentMarkersInsideStrings() {
        String out = processor.normalise("String url = \"http://x.com\"; char c = '/';");
        assertTrue(out.contains("\"http://x.com\""));
    }

    @Test
    void dropsImportAndPackageLines() {
        String out = processor.normalise("package a.b;\nimport java.util.*;\nimport static java.lang.Math.*;\nclass A {}");
        assertEquals("class A {}", out);
    }

    @Test
    void collapsesWhitespaceAndBlankLines() {
        assertEquals("int a = 1;\nint b = 2;", processor.normalise("   int   a\t=  1;  \n\n\n  int b = 2;   "));
    }

    @Test
    void normalisesACodeFile() {
        CodeFile file = new CodeFile(Path.of("A.java"), "class A {} // hi");
        assertEquals("class A {}", processor.normalise(file));
    }

    @Test
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> processor.normalise((String) null));
        assertThrows(IllegalArgumentException.class, () -> processor.normalise((CodeFile) null));
    }
}
