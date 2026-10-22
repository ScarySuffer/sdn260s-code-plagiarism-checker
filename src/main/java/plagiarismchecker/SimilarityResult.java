package plagiarismchecker;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The outcome of comparing two files: the files, a similarity percentage, any matching
 * fragments, and whether the pair is flagged for a person to review.
 *
 * <p>A flag is not a finding of plagiarism. It only says the pair is similar enough to
 * be worth a closer look.</p>
 */
public final class SimilarityResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Orders results from most to least similar. */
    public static final Comparator<SimilarityResult> HIGHEST_FIRST =
            Comparator.comparingDouble(SimilarityResult::getSimilarityPercent).reversed();

    private final CodeFile fileA;
    private final CodeFile fileB;
    private final double similarityPercent;
    private final List<String> matchingFragments;
    private final boolean flagged;

    public SimilarityResult(CodeFile fileA, CodeFile fileB, double similarityPercent,
                            List<String> matchingFragments, boolean flagged) {
        if (similarityPercent < 0 || similarityPercent > 100 || Double.isNaN(similarityPercent)) {
            throw new IllegalArgumentException("Similarity must be between 0 and 100: " + similarityPercent);
        }
        this.fileA = Objects.requireNonNull(fileA, "fileA");
        this.fileB = Objects.requireNonNull(fileB, "fileB");
        this.similarityPercent = similarityPercent;
        this.matchingFragments = Collections.unmodifiableList(new ArrayList<>(matchingFragments));
        this.flagged = flagged;
    }

    public CodeFile getFileA() {
        return fileA;
    }

    public CodeFile getFileB() {
        return fileB;
    }

    public double getSimilarityPercent() {
        return similarityPercent;
    }

    public List<String> getMatchingFragments() {
        return matchingFragments;
    }

    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s <-> %s: %.1f%%%s", fileA.getName(), fileB.getName(),
                similarityPercent, flagged ? " (flagged for review)" : "");
    }

    /** Checks a result read from a file as carefully as one built in code. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (fileA == null || fileB == null || matchingFragments == null
                || Double.isNaN(similarityPercent) || similarityPercent < 0 || similarityPercent > 100) {
            throw new InvalidObjectException("Invalid saved result");
        }
    }
}
