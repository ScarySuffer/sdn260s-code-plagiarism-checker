package plagiarismchecker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * The Swing window for choosing files or folders and viewing results.
 *
 * <p>It calls the same {@link ComparisonService} as the command line, so the comparison
 * logic is never duplicated in the user interface. Loading and comparing run on a
 * background thread so the window stays responsive.</p>
 */
public class GUI {

    private static final Color FLAG_COLOUR = new Color(0xFFE9B8);

    private final Logger logger;
    private final ComparisonService service;
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private final List<CodeFile> files = new ArrayList<>();
    private List<SimilarityResult> results = new ArrayList<>();

    private final DefaultListModel<String> fileListModel = new DefaultListModel<>();
    private final ResultsTableModel tableModel = new ResultsTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextArea detail = new JTextArea();
    private final JLabel fileCount = new JLabel("No files loaded");
    private final JLabel status = new JLabel(" ");
    private final List<JButton> buttons = new ArrayList<>();
    private JFrame frame;

    public GUI() {
        this(new Logger(Path.of("logs", "plagiarism-checker.log"), false));
    }

    public GUI(Logger logger) {
        this.logger = logger;
        this.service = new ComparisonService(logger);
    }

    /** Opens the window. Safe to call from any thread. */
    public void show() {
        SwingUtilities.invokeLater(this::build);
    }

    private void build() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not use the system look and feel: " + e.getMessage());
        }

        frame = new JFrame("Code Plagiarism Checker - SDN260S Group 8");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));
        ((JPanel) frame.getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        frame.add(buildToolbar(), BorderLayout.NORTH);
        frame.add(buildCentre(), BorderLayout.CENTER);

        status.setText("Add files or a folder, then press Compare.");
        status.setForeground(Color.DARK_GRAY);
        frame.add(status, BorderLayout.SOUTH);

        frame.setMinimumSize(new Dimension(800, 520));
        frame.setSize(1050, 680);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.add(button("Add files...", e -> chooseFiles()));
        bar.add(button("Add folder...", e -> chooseFolder()));
        bar.add(button("Clear", e -> clearAll()));
        bar.add(button("Compare", e -> compare()));
        bar.add(button("Export report...", e -> export()));
        bar.add(button("Save results...", e -> saveResults()));
        bar.add(button("Load results...", e -> loadResults()));
        return bar;
    }

    private JButton button(String text, java.awt.event.ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        buttons.add(b);
        return b;
    }

    private JSplitPane buildCentre() {
        JList<String> fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel left = new JPanel(new BorderLayout(0, 4));
        left.add(fileCount, BorderLayout.NORTH);
        left.add(new JScrollPane(fileList), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(250, 100));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        TableRowSorter<ResultsTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(2, SortOrder.DESCENDING)));
        table.setRowSorter(sorter);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void setValue(Object value) {
                setText(value instanceof Double ? String.format(Locale.ROOT, "%.1f", (Double) value)
                        : String.valueOf(value));
                setHorizontalAlignment(value instanceof Double ? RIGHT : LEFT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                if (!selected) {
                    SimilarityResult r = tableModel.getResultAt(t.convertRowIndexToModel(row));
                    c.setBackground(r.isFlagged() ? FLAG_COLOUR : Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Double.class, renderer);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail();
            }
        });

        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        detail.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detail.setText("Select a pair above to see the matching parts.");

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(detail));
        right.setResizeWeight(0.5);
        right.setDividerLocation(260);

        JSplitPane all = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        all.setResizeWeight(0.0);
        return all;
    }

    // ----- actions -----

    private void chooseFiles() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Java files (*.java)", "java"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            load(Arrays.asList(chooser.getSelectedFiles()));
        }
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            load(List.of(chooser.getSelectedFile()));
        }
    }

    private void load(List<File> selected) {
        List<Path> paths = new ArrayList<>();
        for (File f : selected) {
            paths.add(f.toPath());
        }
        setBusy(true, "Loading...");
        new SwingWorker<List<CodeFile>, Void>() {
            @Override
            protected List<CodeFile> doInBackground() {
                return service.load(paths);
            }

            @Override
            protected void done() {
                try {
                    Set<CodeFile> merged = new LinkedHashSet<>(files);
                    merged.addAll(get());
                    files.clear();
                    files.addAll(merged);
                    refreshFileList();
                    setBusy(false, files.isEmpty()
                            ? "No usable .java files were found."
                            : "Loaded " + files.size() + " file(s). Press Compare.");
                } catch (InterruptedException | ExecutionException e) {
                    fail("Could not load files", e);
                }
            }
        }.execute();
    }

    private void compare() {
        if (files.size() < 2) {
            JOptionPane.showMessageDialog(frame, "Add at least two .java files to compare.",
                    "Nothing to compare", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<CodeFile> snapshot = new ArrayList<>(files);
        setBusy(true, "Comparing...");
        new SwingWorker<List<SimilarityResult>, Void>() {
            @Override
            protected List<SimilarityResult> doInBackground() {
                return service.compare(snapshot);
            }

            @Override
            protected void done() {
                try {
                    results = get();
                    tableModel.setResults(results);
                    long flagged = results.stream().filter(SimilarityResult::isFlagged).count();
                    setBusy(false, String.format(Locale.ROOT,
                            "%d pair(s) compared, %d flagged for review. Method: PROPOSED (%d-token groups, flag at %.0f%%). "
                                    + "A flag is not a finding of plagiarism.",
                            results.size(), flagged, service.getChecker().getGramSize(),
                            service.getChecker().getThresholdPercent()));
                    if (!results.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    fail("Comparison failed", e);
                }
            }
        }.execute();
    }

    private void clearAll() {
        files.clear();
        results = new ArrayList<>();
        tableModel.setResults(results);
        refreshFileList();
        detail.setText("Select a pair above to see the matching parts.");
        status.setText("Cleared. Add files or a folder.");
    }

    private void export() {
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Run a comparison first, then export the report.",
                    "Nothing to export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File("plagiarism-report.txt"));
        chooser.setFileFilter(new FileNameExtensionFilter("Text or CSV report (*.txt, *.csv)", "txt", "csv"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        String lower = target.getName().toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".txt") && !lower.endsWith(".csv")) {
            target = new File(target.getParentFile(), target.getName() + ".txt");
        }
        try {
            reportGenerator.writeReport(results, target.toPath());
            logger.info("Report written to " + target);
            status.setText("Report saved: " + target.getAbsolutePath());
        } catch (IOException e) {
            fail("Could not save the report", e);
        }
    }

    private void saveResults() {
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Run a comparison first, then save the results.",
                    "Nothing to save", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setSelectedFile(new File("results.g8"));
        chooser.setFileFilter(new FileNameExtensionFilter("Saved results (*.g8)", "g8"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        if (!target.getName().toLowerCase(Locale.ROOT).endsWith(".g8")) {
            target = new File(target.getParentFile(), target.getName() + ".g8");
        }
        try {
            ResultStore.save(results, target.toPath());
            logger.info("Results saved to " + target);
            status.setText("Results saved: " + target.getAbsolutePath());
        } catch (IOException e) {
            fail("Could not save the results", e);
        }
    }

    private void loadResults() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setFileFilter(new FileNameExtensionFilter("Saved results (*.g8)", "g8"));
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            results = ResultStore.load(chooser.getSelectedFile().toPath());
            tableModel.setResults(results);
            long flagged = results.stream().filter(SimilarityResult::isFlagged).count();
            status.setText(String.format(Locale.ROOT, "Loaded %d saved pair(s), %d flagged for review. "
                    + "A flag is not a finding of plagiarism.", results.size(), flagged));
            logger.info("Results loaded from " + chooser.getSelectedFile());
            if (!results.isEmpty()) {
                table.setRowSelectionInterval(0, 0);
            }
        } catch (IOException e) {
            fail("Could not load the results", e);
        }
    }

    // ----- helpers -----

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        SimilarityResult r = tableModel.getResultAt(table.convertRowIndexToModel(row));
        StringBuilder sb = new StringBuilder();
        sb.append(r).append("\n\n");
        if (r.getMatchingFragments().isEmpty()) {
            sb.append("No matching parts.");
        } else {
            sb.append("Matching parts (as normalised tokens: ID = any name, NUM = a number, STR = text):\n");
            for (String fragment : r.getMatchingFragments()) {
                sb.append("  - ").append(fragment).append('\n');
            }
        }
        detail.setText(sb.toString());
        detail.setCaretPosition(0);
    }

    private void refreshFileList() {
        fileListModel.clear();
        for (CodeFile f : files) {
            fileListModel.addElement(f.getName() + "  (" + f.getLineCount() + " lines)");
        }
        fileCount.setText(files.isEmpty() ? "No files loaded" : files.size() + " file(s) loaded");
    }

    private void setBusy(boolean busy, String message) {
        for (JButton b : buttons) {
            b.setEnabled(!busy);
        }
        status.setText(message);
    }

    private void fail(String message, Exception e) {
        Throwable cause = e instanceof ExecutionException && e.getCause() != null ? e.getCause() : e;
        logger.error(message + ": " + cause.getMessage());
        setBusy(false, message + ".");
        JOptionPane.showMessageDialog(frame, message + ":\n" + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
