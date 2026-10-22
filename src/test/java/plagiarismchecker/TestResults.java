package plagiarismchecker;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Test helper: builds results with repeated similarity values to check ordering of ties. */
final class TestResults {

    private TestResults() {
    }

    static List<SimilarityResult> results(int count) {
        Random random = new Random(7);
        CodeFile a = new CodeFile(Paths.get("A.java"), "class A {}");
        List<SimilarityResult> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CodeFile b = new CodeFile(Paths.get("B" + i + ".java"), "class B" + i + " {}");
            out.add(new SimilarityResult(a, b, random.nextInt(5) * 10.0, Collections.<String>emptyList(), false));
        }
        return out;
    }
}
