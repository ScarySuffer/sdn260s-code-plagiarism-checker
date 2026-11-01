package plagiarismchecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Compares tokenised code and calculates a similarity percentage.
 *
 * <p>Method (approved by the group on 1 Nov 2026): each file's tokens
 * are turned into a set of overlapping {@code n}-token groups (n-grams, default n = 3).
 * The similarity is the Jaccard index of the two sets:
 * {@code |A and B| / |A or B| x 100}. A pair is flagged for human review when the
 * similarity is at or above the threshold (default 70%). Both defaults are starting
 * points to be tuned on test data, not final values.</p>
 *
 * <p>A flag is not a finding of plagiarism. It only marks a pair worth a closer look.</p>
 */
public class SimilarityChecker {

    /** Default n-gram size. */
    public static final int DEFAULT_GRAM_SIZE = 3;
    /** Default flag threshold, in percent. */
    public static final double DEFAULT_THRESHOLD_PERCENT = 70.0;

    private final int gramSize;
    private final double thresholdPercent;

    public SimilarityChecker() {
        this(DEFAULT_GRAM_SIZE, DEFAULT_THRESHOLD_PERCENT);
    }

    public SimilarityChecker(int gramSize, double thresholdPercent) {
        if (gramSize < 1) {
            throw new IllegalArgumentException("gramSize must be at least 1: " + gramSize);
        }
        if (Double.isNaN(thresholdPercent) || thresholdPercent < 0 || thresholdPercent > 100) {
            throw new IllegalArgumentException("threshold must be between 0 and 100: " + thresholdPercent);
        }
        this.gramSize = gramSize;
        this.thresholdPercent = thresholdPercent;
    }

    public int getGramSize() {
        return gramSize;
    }

    public double getThresholdPercent() {
        return thresholdPercent;
    }

    /** Compares one pair of files. */
    public SimilarityResult compare(CodeFile a, List<String> tokensA, CodeFile b, List<String> tokensB) {
        if (a == null || b == null || tokensA == null || tokensB == null) {
            throw new IllegalArgumentException("files and token lists must not be null");
        }
        List<String> gramsA = grams(tokensA);
        List<String> gramsB = grams(tokensB);
        Set<String> setA = new LinkedHashSet<>(gramsA);
        Set<String> setB = new HashSet<>(gramsB);

        Set<String> common = new HashSet<>(setA);
        common.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        double percent = union.isEmpty() ? 0.0 : 100.0 * common.size() / union.size();
        return new SimilarityResult(a, b, percent, fragments(tokensA, gramsA, common),
                percent >= thresholdPercent);
    }

    /** Compares every pair of the given files; results are ordered most similar first. */
    public List<SimilarityResult> compareAll(List<CodeFile> files, CodeProcessor processor, CodeTokenizer tokenizer) {
        List<List<String>> tokens = new ArrayList<>();
        for (CodeFile file : files) {
            tokens.add(tokenizer.tokenise(processor.normalise(file)));
        }
        List<SimilarityResult> results = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            for (int j = i + 1; j < files.size(); j++) {
                results.add(compare(files.get(i), tokens.get(i), files.get(j), tokens.get(j)));
            }
        }
        results.sort(SimilarityResult.HIGHEST_FIRST);
        return results;
    }

    /**
     * Same as {@link #compareAll(List, CodeProcessor, CodeTokenizer)}, but the pairs are compared
     * on several threads. The results and their order are identical; only the time changes.
     *
     * <p>Each pair is independent and {@link #compare} only uses its own local data, so no
     * locking is needed. The results are collected in pair order into a {@link RankedList}.</p>
     *
     * @param threads number of worker threads; 1 or less uses the single-threaded method
     */
    public List<SimilarityResult> compareAll(List<CodeFile> files, CodeProcessor processor,
                                             CodeTokenizer tokenizer, int threads) {
        if (threads <= 1) {
            return compareAll(files, processor, tokenizer);
        }
        List<List<String>> tokens = new ArrayList<>();
        for (CodeFile file : files) {
            tokens.add(tokenizer.tokenise(processor.normalise(file)));
        }
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<SimilarityResult>> futures = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                for (int j = i + 1; j < files.size(); j++) {
                    Pair<Integer, Integer> pair = Pair.of(i, j);
                    Callable<SimilarityResult> task = () -> compare(files.get(pair.getFirst()),
                            tokens.get(pair.getFirst()), files.get(pair.getSecond()), tokens.get(pair.getSecond()));
                    futures.add(pool.submit(task));
                }
            }
            RankedList<SimilarityResult> ranked = new RankedList<>(SimilarityResult.HIGHEST_FIRST);
            for (Future<SimilarityResult> future : futures) {
                ranked.add(future.get());
            }
            return ranked.toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Comparison was interrupted", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Comparison failed: " + e.getCause(), e.getCause());
        } finally {
            pool.shutdownNow();
        }
    }

    /** Overlapping n-token groups, each written as its tokens joined by spaces. */
    private List<String> grams(List<String> tokens) {
        List<String> grams = new ArrayList<>();
        if (tokens.isEmpty()) {
            return grams;
        }
        int size = Math.min(gramSize, tokens.size());
        for (int i = 0; i + size <= tokens.size(); i++) {
            grams.add(String.join(" ", tokens.subList(i, i + size)));
        }
        return grams;
    }

    /** Joins neighbouring shared groups into longer matching runs of tokens (from file A). */
    private List<String> fragments(List<String> tokensA, List<String> gramsA, Set<String> common) {
        List<String> fragments = new ArrayList<>();
        int size = Math.min(gramSize, tokensA.size());
        int i = 0;
        while (i < gramsA.size()) {
            if (!common.contains(gramsA.get(i))) {
                i++;
                continue;
            }
            int start = i;
            while (i < gramsA.size() && common.contains(gramsA.get(i))) {
                i++;
            }
            fragments.add(String.join(" ", tokensA.subList(start, i - 1 + size)));
        }
        return fragments;
    }
}
