package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ResultsTableModelTest {

    @Test
    void exposesResultsAsRows() {
        CodeFile a = new CodeFile(Path.of("A.java"), "x");
        CodeFile b = new CodeFile(Path.of("B.java"), "y");
        ResultsTableModel model = new ResultsTableModel();
        assertEquals(0, model.getRowCount());

        model.setResults(Arrays.asList(
                new SimilarityResult(a, b, 87.54, Collections.<String>emptyList(), true),
                new SimilarityResult(b, a, 10.0, Collections.<String>emptyList(), false)));

        assertEquals(2, model.getRowCount());
        assertEquals(4, model.getColumnCount());
        assertEquals("A.java", model.getValueAt(0, 0));
        assertEquals("B.java", model.getValueAt(0, 1));
        assertEquals(87.5, model.getValueAt(0, 2));
        assertEquals("Flagged for review", model.getValueAt(0, 3));
        assertEquals("Not flagged", model.getValueAt(1, 3));
        assertEquals(Double.class, model.getColumnClass(2));
        assertEquals("Similarity (%)", model.getColumnName(2));
    }
}
