package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoggerTest {

    @TempDir
    Path dir;

    @Test
    void keepsEntriesAndCountsThemByLevel() {
        Logger logger = new Logger(null, false);
        logger.info("a");
        logger.warn("b");
        logger.warn("c");
        logger.error("d");

        assertEquals(4, logger.getEntries().size());
        assertEquals(1, logger.count(Logger.Level.INFO));
        assertEquals(2, logger.count(Logger.Level.WARN));
        assertEquals(1, logger.count(Logger.Level.ERROR));
    }

    @Test
    void appendsToAFileAndCreatesMissingFolders() throws Exception {
        Path file = dir.resolve("logs").resolve("run.log");
        Logger logger = new Logger(file, false);

        logger.info("first");
        logger.error("second");

        String text = Files.readString(file);
        assertTrue(text.contains("[INFO] first"));
        assertTrue(text.contains("[ERROR] second"));
    }
}
