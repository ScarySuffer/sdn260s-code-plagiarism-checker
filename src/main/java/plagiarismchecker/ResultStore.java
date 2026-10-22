package plagiarismchecker;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves comparison results to a file and loads them back, using Java object serialization.
 *
 * <p>Loading only accepts the classes this program writes, so a file from somewhere else
 * cannot make the program build unexpected objects.</p>
 */
public final class ResultStore {

    /** Only our own classes and the basic Java collection and number types may be read. */
    private static final String ALLOWED =
            "plagiarismchecker.*;java.util.*;java.lang.*;maxdepth=20;maxarray=100000;!*";

    private ResultStore() {
    }

    public static void save(List<SimilarityResult> results, Path target) throws IOException {
        if (results == null || target == null) {
            throw new IllegalArgumentException("results and target must not be null");
        }
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(target))) {
            out.writeObject(new ArrayList<>(results));
        }
    }

    public static List<SimilarityResult> load(Path source) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(source))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter(ALLOWED));
            Object read = in.readObject();
            if (!(read instanceof List)) {
                throw new IOException("This is not a saved results file.");
            }
            List<SimilarityResult> results = new ArrayList<>();
            for (Object item : (List<?>) read) {
                if (!(item instanceof SimilarityResult)) {
                    throw new IOException("This is not a saved results file.");
                }
                results.add((SimilarityResult) item);
            }
            return results;
        } catch (ClassNotFoundException | InvalidClassException | StreamCorruptedException e) {
            throw new IOException("This is not a valid saved results file: " + e.getMessage(), e);
        }
    }
}
