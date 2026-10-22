package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimilarityCheckerTest {

    private final CodeProcessor processor = new CodeProcessor();
    private final CodeTokenizer tokenizer = new CodeTokenizer();
    private final SimilarityChecker checker = new SimilarityChecker();

    private static CodeFile file(String name, String content) {
        return new CodeFile(Path.of(name), content);
    }

    private List<String> tokens(CodeFile f) {
        return tokenizer.tokenise(processor.normalise(f));
    }

    private static final String ORIGINAL =
            "// Adds the numbers 0 to 9\npublic class StudentA {\n public static void main(String[] args) {\n"
            + "  int total = 0;\n  for (int i = 0; i < 10; i++) {\n   total += i;\n  }\n"
            + "  System.out.println(total);\n }\n}\n";
    private static final String RENAMED =
            "public class StudentB {\n public static void main(String[] args) {\n int result=0;\n"
            + " for(int n=0;n<10;n++){\n result+=n;\n }\n System.out.println(result);\n }\n}\n";
    private static final String UNRELATED =
            "import java.util.Scanner;\npublic class StudentC {\n public static void main(String[] args) {\n"
            + "  Scanner in = new Scanner(System.in);\n  System.out.print(\"Name: \");\n"
            + "  String name = in.nextLine();\n  if (name.length() > 2) {\n"
            + "   System.out.println(\"Hello \" + name);\n  }\n }\n}\n";

    @Test
    void renamedCopyScoresHundredAndIsFlagged() {
        CodeFile a = file("StudentA.java", ORIGINAL);
        CodeFile b = file("StudentB.java", RENAMED);
        SimilarityResult r = checker.compare(a, tokens(a), b, tokens(b));
        assertEquals(100.0, r.getSimilarityPercent(), 0.0001);
        assertTrue(r.isFlagged());
        assertFalse(r.getMatchingFragments().isEmpty());
    }

    @Test
    void unrelatedProgramsScoreLowAndAreNotFlagged() {
        CodeFile a = file("StudentA.java", ORIGINAL);
        CodeFile c = file("StudentC.java", UNRELATED);
        SimilarityResult r = checker.compare(a, tokens(a), c, tokens(c));
        assertTrue(r.getSimilarityPercent() < 50.0, "was " + r.getSimilarityPercent());
        assertFalse(r.isFlagged());
    }

    @Test
    void comparisonIsSymmetric() {
        CodeFile a = file("StudentA.java", ORIGINAL);
        CodeFile c = file("StudentC.java", UNRELATED);
        assertEquals(checker.compare(a, tokens(a), c, tokens(c)).getSimilarityPercent(),
                checker.compare(c, tokens(c), a, tokens(a)).getSimilarityPercent(), 0.0001);
    }

    @Test
    void emptyFilesScoreZero() {
        CodeFile a = file("A.java", "");
        CodeFile b = file("B.java", "");
        SimilarityResult r = checker.compare(a, Collections.<String>emptyList(), b, Collections.<String>emptyList());
        assertEquals(0.0, r.getSimilarityPercent(), 0.0001);
        assertFalse(r.isFlagged());
    }

    @Test
    void veryShortFilesStillCompare() {
        CodeFile a = file("A.java", "x");
        CodeFile b = file("B.java", "y");
        List<String> t = Arrays.asList("ID", ";");
        assertEquals(100.0, checker.compare(a, t, b, t).getSimilarityPercent(), 0.0001);
    }

    @Test
    void thresholdDecidesTheFlag() {
        CodeFile a = file("A.java", "x");
        CodeFile b = file("B.java", "y");
        List<String> t1 = Arrays.asList("a", "b", "c", "d");
        List<String> t2 = Arrays.asList("a", "b", "c", "e");
        double percent = checker.compare(a, t1, b, t2).getSimilarityPercent();
        assertEquals(33.33, percent, 0.01);
        assertFalse(new SimilarityChecker(3, 50).compare(a, t1, b, t2).isFlagged());
        assertTrue(new SimilarityChecker(3, 30).compare(a, t1, b, t2).isFlagged());
    }

    @Test
    void compareAllOrdersMostSimilarFirst() {
        List<CodeFile> files = Arrays.asList(file("StudentA.java", ORIGINAL),
                file("StudentC.java", UNRELATED), file("StudentB.java", RENAMED));
        List<SimilarityResult> results = checker.compareAll(files, processor, tokenizer);
        assertEquals(3, results.size());
        assertEquals("StudentA.java", results.get(0).getFileA().getName());
        assertEquals("StudentB.java", results.get(0).getFileB().getName());
        assertTrue(results.get(0).getSimilarityPercent() >= results.get(1).getSimilarityPercent());
        assertTrue(results.get(1).getSimilarityPercent() >= results.get(2).getSimilarityPercent());
    }

    @Test
    void rejectsBadSettingsAndNulls() {
        assertThrows(IllegalArgumentException.class, () -> new SimilarityChecker(0, 50));
        assertThrows(IllegalArgumentException.class, () -> new SimilarityChecker(3, 101));
        assertThrows(IllegalArgumentException.class, () -> new SimilarityChecker(3, -1));
        assertThrows(IllegalArgumentException.class, () -> checker.compare(null, Collections.<String>emptyList(),
                null, Collections.<String>emptyList()));
    }
}
