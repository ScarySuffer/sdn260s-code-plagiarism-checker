package plagiarismchecker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Loads files and folders. A path that cannot be used is logged and skipped; a file
     * given twice is only counted once.
     */
    public List<CodeFile> load(Collection<Path> paths) {
        Set<CodeFile> loaded = new LinkedHashSet<>();
        for (Path path : paths) {
            try {
                if (Files.isDirectory(path)) {
                    loaded.addAll(reader.scanFolder(path));
                } else {
                    loaded.add(reader.readFile(path));
                }
            } catch (InvalidSourceFileException e) {
                logger.error(e.getMessage());
            }
        }
        return new ArrayList<>(loaded);
    }

    /** Compares every pair of files; results are ordered most similar first. */
    public List<SimilarityResult> compare(List<CodeFile> files) {
        List<SimilarityResult> results = checker.compareAll(files, processor, tokenizer, threads);
        long flagged = results.stream().filter(SimilarityResult::isFlagged).count();
        logger.info("Compared " + files.size() + " file(s): " + results.size() + " pair(s), "
                + flagged + " flagged for review (" + threads + " thread(s)).");
        return results;
    }
}
