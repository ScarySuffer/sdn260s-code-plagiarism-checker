package plagiarismchecker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Connects the components (reader, processor, tokenizer, checker) so that every user
 * interface (command line, menu and Swing window) runs exactly the same logic.
 */
public class ComparisonService {

    private final Logger logger;
    private final FileReader reader;
    private final CodeProcessor processor = new CodeProcessor();
    private final CodeTokenizer tokenizer = new CodeTokenizer();
    private final SimilarityChecker checker;
    private final int threads;
    private volatile long lastComparisonNanos;

    public ComparisonService(Logger logger) {
        this(logger, new SimilarityChecker());
    }

    public ComparisonService(Logger logger, SimilarityChecker checker) {
        this(logger, checker, Math.max(1, Math.min(8, Runtime.getRuntime().availableProcessors())));
    }

    /** @param threads worker threads used to compare pairs (1 = single-threaded) */
    public ComparisonService(Logger logger, SimilarityChecker checker, int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be at least 1: " + threads);
        }
        if (logger == null || checker == null) {
            throw new IllegalArgumentException("logger and checker must not be null");
        }
        this.logger = logger;
        this.reader = new FileReader(logger);
        this.checker = checker;
        this.threads = threads;
    }

    public int getThreads() {
        return threads;
    }

    public SimilarityChecker getChecker() {
        return checker;
    }

    /** Duration of the most recent comparison, measured with a monotonic clock. */
    public long getLastComparisonMillis() {
        return lastComparisonNanos / 1_000_000L;
    }

    /**
     * Loads files and folders. A path that cannot be used is logged and skipped; a file
     * given twice is only counted once.
     */
    public List<CodeFile> load(Collection<Path> paths) {
        return load(paths, message -> { });
    }

    /**
     * Same as {@link #load(Collection)}, but every path or file that is rejected is also
     * reported to {@code rejectionHandler} with the reason (shown on screen by the menu).
     */
    public List<CodeFile> load(Collection<Path> paths, Consumer<String> rejectionHandler) {
        Set<CodeFile> loaded = new LinkedHashSet<>();
        for (Path path : paths) {
            try {
                if (Files.isDirectory(path)) {
                    loaded.addAll(reader.scanFolder(path, rejectionHandler));
                } else {
                    loaded.add(reader.readFile(path));
                }
            } catch (InvalidSourceFileException e) {
                logger.error(e.getMessage());
                rejectionHandler.accept("File rejected: " + e.getMessage());
            }
        }
        return new ArrayList<>(loaded);
    }

    /**
     * Names of files that contain no code once comments are removed (for example a file
     * with only comments). Such a file scores 0.0% against every other file, so the user
     * is warned instead of being left to wonder.
     */
    public List<String> filesWithNoCode(List<CodeFile> files) {
        List<String> names = new ArrayList<>();
        if (files == null) {
            return names;
        }
        for (CodeFile file : files) {
            if (tokenizer.tokenise(processor.normalise(file)).isEmpty()) {
                names.add(file.getPath().getFileName().toString());
            }
        }
        return names;
    }

    /** Compares every pair of files; results are ordered most similar first. */
    public List<SimilarityResult> compare(List<CodeFile> files) {
        if (files == null || files.size() < 2) {
            lastComparisonNanos = 0L;
            logger.warn("Nothing to compare: load at least two usable .java files.");
            return new ArrayList<>();
        }
        for (String name : filesWithNoCode(files)) {
            logger.warn("No code to compare in " + name + " (only comments or blank lines); it scores 0.0%.");
        }
        long start = System.nanoTime();
        List<SimilarityResult> results = checker.compareAll(files, processor, tokenizer, threads);
        lastComparisonNanos = System.nanoTime() - start;
        long flagged = results.stream().filter(SimilarityResult::isFlagged).count();
        logger.info("Compared " + files.size() + " file(s): " + results.size() + " pair(s), "
                + flagged + " flagged for review (" + threads + " thread(s)); elapsed "
                + String.format(java.util.Locale.ROOT, "%.3f", lastComparisonNanos / 1_000_000.0) + " ms.");
        return results;
    }
}
