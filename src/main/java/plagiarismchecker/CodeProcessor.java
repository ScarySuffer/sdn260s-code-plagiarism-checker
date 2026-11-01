package plagiarismchecker;

import java.util.ArrayList;
import java.util.List;

/**
 * Prepares source code for comparison by removing comments and reducing formatting
 * differences, while keeping the structure of the code.
 *
 * <p>Rules (approved by the group on 1 Nov 2026):</p>
 * <ul>
 *   <li>Line comments and block comments are removed.
 *       Comment markers inside string or character literals are left alone.</li>
 *   <li>{@code package} and {@code import} lines are dropped, because they are shared
 *       boilerplate and would otherwise make unrelated files look similar.</li>
 *   <li>Runs of whitespace become a single space and blank lines are removed.</li>
 * </ul>
 */
public class CodeProcessor {

    /** @return the normalised text of the given file */
    public String normalise(CodeFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        return normalise(file.getContent());
    }

    /** @return the normalised version of the given source text */
    public String normalise(String source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        String withoutComments = stripComments(source);
        List<String> kept = new ArrayList<>();
        for (String line : withoutComments.split("\\R")) {
            String cleaned = line.trim().replaceAll("\\s+", " ");
            if (cleaned.isEmpty() || isBoilerplate(cleaned)) {
                continue;
            }
            kept.add(cleaned);
        }
        return String.join("\n", kept);
    }

    private static boolean isBoilerplate(String line) {
        return line.startsWith("import ") || line.startsWith("package ");
    }

    /** Removes comments but keeps string, character and text-block literals intact. */
    static String stripComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        int n = s.length();
        while (i < n) {
            char c = s.charAt(i);
            char next = i + 1 < n ? s.charAt(i + 1) : '\0';
            if (c == '/' && next == '/') {
                while (i < n && s.charAt(i) != '\n' && s.charAt(i) != '\r') {
                    i++;
                }
            } else if (c == '/' && next == '*') {
                int end = s.indexOf("*/", i + 2);
                i = end < 0 ? n : end + 2;
                out.append(' ');
            } else if (s.startsWith("\"\"\"", i)) {
                int end = s.indexOf("\"\"\"", i + 3);
                int stop = end < 0 ? n : end + 3;
                out.append(s, i, stop);
                i = stop;
            } else if (c == '"' || c == '\'') {
                int start = i++;
                while (i < n && s.charAt(i) != c && s.charAt(i) != '\n') {
                    i += s.charAt(i) == '\\' ? 2 : 1;
                }
                i = Math.min(n, i + 1);
                out.append(s, start, i);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
