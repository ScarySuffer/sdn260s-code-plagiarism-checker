package plagiarismchecker;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Represents one submitted source-code file: where it came from and what it contains.
 *
 * <p>Instances are immutable. Two {@code CodeFile}s are equal when they point to the
 * same file on disk, so a file loaded twice is only counted once in a collection.</p>
 */
public final class CodeFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Not serialisable (a {@code Path} is not), so it is written through a small proxy instead. */
    private final transient Path path;
    private final String content;

    /**
     * @param path    the file's location (made absolute and normalised)
     * @param content the full text of the file
     */
    public CodeFile(Path path, String content) {
        this.path = Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
        this.content = Objects.requireNonNull(content, "content");
    }

    public Path getPath() {
        return path;
    }

    /** @return just the file name, for example {@code StudentA.java} */
    public String getName() {
        return path.getFileName().toString();
    }

    public String getContent() {
        return content;
    }

    /** @return the number of lines in the file */
    public int getLineCount() {
        return (int) content.lines().count();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CodeFile)) {
            return false;
        }
        return path.equals(((CodeFile) other).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return getName() + " (" + getLineCount() + " lines)";
    }

    // ----- serialization (proxy pattern, as recommended in Effective Java) -----

    private Object writeReplace() {
        return new SerialProxy(path.toString(), content);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static final class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String pathText;
        private final String content;

        SerialProxy(String pathText, String content) {
            this.pathText = pathText;
            this.content = content;
        }

        private Object readResolve() throws InvalidObjectException {
            if (pathText == null || content == null) {
                throw new InvalidObjectException("Missing file data");
            }
            return new CodeFile(Paths.get(pathText), content);
        }
    }
}
