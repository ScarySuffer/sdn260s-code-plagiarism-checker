package plagiarismchecker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records important events, warnings and errors.
 *
 * <p>Every entry is kept in memory (so the UI or tests can read it) and can also be
 * echoed to the console and appended to a log file. A problem writing the log file
 * never stops the application; it is reported once on standard error.</p>
 */
public class Logger {

    /** How serious a log entry is. */
    public enum Level { INFO, WARN, ERROR }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;
    private final boolean echoToConsole;
    private final List<String> entries = new ArrayList<>();
    private boolean fileFailureReported;

    /**
     * @param logFile       file to append to, or {@code null} for no file
     * @param echoToConsole whether to also print each entry
     */
    public Logger(Path logFile, boolean echoToConsole) {
        this.logFile = logFile;
        this.echoToConsole = echoToConsole;
    }

    /** @return a logger that only prints to the console */
    public static Logger console() {
        return new Logger(null, true);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warn(String message) {
        log(Level.WARN, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    /** @return a read-only copy of every entry recorded so far */
    public synchronized List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /** @return how many entries were recorded at the given level */
    public synchronized long count(Level level) {
        String tag = "[" + level + "]";
        return entries.stream().filter(e -> e.contains(tag)).count();
    }

    private synchronized void log(Level level, String message) {
        String line = LocalDateTime.now().format(TIME) + " [" + level + "] " + message;
        entries.add(line);

        if (echoToConsole) {
            (level == Level.INFO ? System.out : System.err).println(line);
        }
        if (logFile != null) {
            writeToFile(line);
        }
    }

    private void writeToFile(String line) {
        try {
            Path parent = logFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(logFile, (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            if (!fileFailureReported) {
                fileFailureReported = true;
                System.err.println("Could not write to log file " + logFile + ": " + e.getMessage());
            }
        }
    }
}
