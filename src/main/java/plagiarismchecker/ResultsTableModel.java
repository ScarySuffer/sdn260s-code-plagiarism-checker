package plagiarismchecker;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Table model for the results grid in the {@link GUI}. Kept separate so it can be tested. */
public class ResultsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = {"File A", "File B", "Similarity (%)", "Status"};

    private final transient List<SimilarityResult> results = new ArrayList<>();

    public void setResults(List<SimilarityResult> newResults) {
        results.clear();
        results.addAll(newResults);
        fireTableDataChanged();
    }

    public SimilarityResult getResultAt(int row) {
        return results.get(row);
    }

    @Override
    public int getRowCount() {
        return results.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 2 ? Double.class : String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        SimilarityResult r = results.get(row);
        switch (column) {
            case 0:
                return r.getFileA().getName();
            case 1:
                return r.getFileB().getName();
            case 2:
                return Math.round(r.getSimilarityPercent() * 10.0) / 10.0;
            default:
                return r.isFlagged() ? "Flagged for review" : "Not flagged";
        }
    }
}
