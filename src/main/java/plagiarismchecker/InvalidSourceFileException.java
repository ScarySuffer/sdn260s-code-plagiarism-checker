package plagiarismchecker;

import java.nio.file.Path;

/**
 * Thrown when a file or folder cannot be used as a source-code submission
 * (missing, empty, wrong type, unreadable, and so on).
 *
 * <p>The message is written for the user, so the CLI and GUI can show it as is.</p>
 */
public class InvalidSourceFileException extends Exception {

    private static final long serialVersionUID = 1L;

    private final transient Path path;

    public InvalidSourceFileException(String message, Path path) {
        super(message);
        this.path = path;
    }

    public InvalidSourceFileException(String message, Path path, Throwable cause) {
        super(message, cause);
        this.path = path;
    }

    /** @return the path that caused the problem, or {@code null} if none was given */
    public Path getPath() {
        return path;
    }
}
