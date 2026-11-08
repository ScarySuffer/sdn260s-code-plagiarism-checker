package plagiarismchecker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads and validates Java source files, and scans folders for them.
 *
 * <p>This class is named {@code FileReader} to match the Check-In 1 design. It is not
 * {@link java.io.FileReader} and does not use it.</p>
 *
 * <p>A single bad file never stops a folder scan: it is skipped and the reason is
 * written to the {@link Logger}.</p>
 */
public class FileReader {

    private static final String EXTENSION = ".java";

    private final Logger logger;

    public FileReader(Logger logger) {
        this.logger = logger;
    }

    /**
     * Reads one source file.
     *
     * @throws InvalidSourceFileException if the file is missing, not a {@code .java} file,
     *                                    unreadable or empty
     */
    public CodeFile readFile(Path path) throws InvalidSourceFileException {
        if (path == null) {
            throw new InvalidSourceFileException("No file was given.", null);
        }
        if (!Files.exists(path)) {
            throw new InvalidSourceFileException("File does not exist: " + path, path);
        }
        if (!Files.isRegularFile(path)) {
            throw new InvalidSourceFileException("This is not a file: " + path, path);
        }
        if (!hasJavaExtension(path)) {
            throw new InvalidSourceFileException(
                    "Only " + EXTENSION + " files can be checked: " + path.getFileName(), path);
        }
        if (!Files.isReadable(path)) {
            throw new InvalidSourceFileException("File cannot be read (permission denied): " + path, path);
        }

        String content = readText(path);
        if (content.startsWith("﻿")) {
            content = content.substring(1); // remove a byte-order mark
        }
        if (content.isBlank()) {
            throw new InvalidSourceFileException("File is empty: " + path.getFileName(), path);
        }

        CodeFile file = new CodeFile(path, content);
        logger.info("Loaded " + file);
        return file;
    }

    /**
     * Finds every {@code .java} file in a folder and all of its sub-folders.
     * Files that cannot be used are skipped and logged.
     *
     * @return the files found, in path order
     * @throws InvalidSourceFileException if the folder itself does not exist or is not a folder
     */
    public List<CodeFile> scanFolder(Path folder) throws InvalidSourceFileException {
        return scanFolder(folder, message -> { });
    }

    /**
     * Same as {@link #scanFolder(Path)}, but every file that is skipped is also reported to
     * {@code rejectionHandler} with the reason, so the menu and the window can show it.
     */
    public List<CodeFile> scanFolder(Path folder, Consumer<String> rejectionHandler)
            throws InvalidSourceFileException {
        if (folder == null || !Files.exists(folder)) {
            throw new InvalidSourceFileException("Folder does not exist: " + folder, folder);
        }
        if (!Files.isDirectory(folder)) {
            throw new InvalidSourceFileException("This is not a folder: " + folder, folder);
        }

        List<CodeFile> found = new ArrayList<>();
        scanRecursively(folder, found, rejectionHandler);
        logger.info("Scanned " + folder + ": found " + found.size() + " source file(s).");
        return found;
    }

    /** Recursion: each sub-folder is scanned by calling this method again. */
    private void scanRecursively(Path folder, List<CodeFile> found, Consumer<String> rejectionHandler) {
        List<Path> entries;
        try (Stream<Path> stream = Files.list(folder)) {
            entries = stream.sorted().collect(Collectors.toList());
        } catch (IOException | SecurityException e) {
            logger.error("Could not open folder " + folder + ": " + e.getMessage());
            return;
        }

        for (Path entry : entries) {
            if (Files.isSymbolicLink(entry)) {
                logger.warn("Skipped symbolic link: " + entry); // avoids loops
            } else if (Files.isDirectory(entry)) {
                scanRecursively(entry, found, rejectionHandler);
            } else if (hasJavaExtension(entry)) {
                try {
                    found.add(readFile(entry));
                } catch (InvalidSourceFileException e) {
                    logger.warn("Skipped " + entry + ": " + e.getMessage());
                    rejectionHandler.accept("File rejected: " + entry + ": " + e.getMessage());
                }
            } else {
                logger.info("Skipped (not a .java file): " + entry);
                rejectionHandler.accept("Skipped (not a .java file): " + entry);
            }
        }
    }

    private String readText(Path path) throws InvalidSourceFileException {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            // Older editors save in a single-byte encoding. Fall back instead of failing.
            try {
                String text = Files.readString(path, StandardCharsets.ISO_8859_1);
                logger.warn(path.getFileName() + " is not valid UTF-8; read as ISO-8859-1.");
                return text;
            } catch (IOException second) {
                throw new InvalidSourceFileException("Could not read file: " + path, path, second);
            }
        } catch (IOException e) {
            throw new InvalidSourceFileException("Could not read file: " + path, path, e);
        }
    }

    private static boolean hasJavaExtension(Path path) {
        Path name = path.getFileName();
        return name != null && name.toString().toLowerCase(Locale.ROOT).endsWith(EXTENSION);
    }
}
