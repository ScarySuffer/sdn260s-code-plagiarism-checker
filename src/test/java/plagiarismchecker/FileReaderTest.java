package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileReaderTest {

    @TempDir
    Path dir;

    private Logger logger;
    private FileReader reader;

    @BeforeEach
    void setUp() {
        logger = new Logger(null, false); // quiet: no console, no file
        reader = new FileReader(logger);
    }

    private Path write(String relative, String content) throws IOException {
        Path file = dir.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return file;
    }

    // ---- normal cases ----

    @Test
    void readsAValidJavaFile() throws Exception {
        Path file = write("A.java", "class A {\n}\n");

        CodeFile code = reader.readFile(file);

        assertEquals("A.java", code.getName());
        assertEquals(2, code.getLineCount());
        assertTrue(code.getContent().contains("class A"));
    }

    @Test
    void scansNestedFoldersAndIgnoresOtherFileTypes() throws Exception {
        write("one/A.java", "class A {}");
        write("one/two/B.java", "class B {}");
        write("one/two/three/C.java", "class C {}");
        write("notes.txt", "not code");

        List<CodeFile> files = reader.scanFolder(dir);

        assertEquals(3, files.size());
    }

    // ---- edge cases and errors ----

    @Test
    void rejectsAMissingFile() {
        InvalidSourceFileException e = assertThrows(InvalidSourceFileException.class,
                () -> reader.readFile(dir.resolve("Missing.java")));
        assertTrue(e.getMessage().contains("does not exist"));
    }

    @Test
    void rejectsAnEmptyFile() throws Exception {
        Path file = write("Empty.java", "");
        InvalidSourceFileException e = assertThrows(InvalidSourceFileException.class,
                () -> reader.readFile(file));
        assertTrue(e.getMessage().contains("empty"));
    }

    @Test
    void rejectsAWhitespaceOnlyFile() throws Exception {
        Path file = write("Blank.java", "   \n\t\n");
        assertThrows(InvalidSourceFileException.class, () -> reader.readFile(file));
    }

    @Test
    void rejectsAFileThatIsNotJava() throws Exception {
        Path file = write("notes.txt", "hello");
        InvalidSourceFileException e = assertThrows(InvalidSourceFileException.class,
                () -> reader.readFile(file));
        assertTrue(e.getMessage().contains(".java"));
    }

    @Test
    void rejectsAFolderPassedAsAFile() {
        assertThrows(InvalidSourceFileException.class, () -> reader.readFile(dir));
    }

    @Test
    void rejectsANullPath() {
        assertThrows(InvalidSourceFileException.class, () -> reader.readFile(null));
    }

    @Test
    void scanRejectsAFileOrMissingFolder() throws Exception {
        Path file = write("A.java", "class A {}");
        assertThrows(InvalidSourceFileException.class, () -> reader.scanFolder(file));
        assertThrows(InvalidSourceFileException.class, () -> reader.scanFolder(dir.resolve("nope")));
    }

    @Test
    void scanSkipsBadFilesButKeepsTheGoodOnes() throws Exception {
        write("Good.java", "class Good {}");
        write("Empty.java", "");

        List<CodeFile> files = reader.scanFolder(dir);

        assertEquals(1, files.size());
        assertEquals("Good.java", files.get(0).getName());
        assertEquals(1, logger.count(Logger.Level.WARN), "the skipped file should be logged as a warning");
    }

    @Test
    void scanOfAnEmptyFolderReturnsNothing() throws Exception {
        assertTrue(reader.scanFolder(dir).isEmpty());
    }

    @Test
    void readsFilesThatAreNotValidUtf8() throws Exception {
        Path file = dir.resolve("Latin.java");
        Files.write(file, "// café\nclass Latin {}".getBytes(StandardCharsets.ISO_8859_1));

        CodeFile code = reader.readFile(file);

        assertTrue(code.getContent().contains("class Latin"));
        assertEquals(1, logger.count(Logger.Level.WARN));
    }

    @Test
    void removesAByteOrderMark() throws Exception {
        Path file = write("Bom.java", "﻿class Bom {}");
        assertEquals("class Bom {}", reader.readFile(file).getContent());
    }

    @Test
    void readsVerySmallAndLargeFiles() throws Exception {
        Path small = write("Small.java", ";");
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            big.append("int v").append(i).append(" = ").append(i).append(";\n");
        }
        Path large = write("Large.java", big.toString());

        assertEquals(1, reader.readFile(small).getLineCount());
        assertEquals(20_000, reader.readFile(large).getLineCount());
    }
}
