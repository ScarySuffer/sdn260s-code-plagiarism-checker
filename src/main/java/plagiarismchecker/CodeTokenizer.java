package plagiarismchecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Splits normalised code into comparable tokens.
 *
 * <p>Token rules (PROPOSED, to be confirmed by the group after testing):</p>
 * <ul>
 *   <li>Java keywords and {@code true}/{@code false}/{@code null} are kept as written.</li>
 *   <li>Every other identifier (variable, method, class name) becomes {@code ID}, so
 *       renaming variables does not hide copied logic.</li>
 *   <li>Number literals become {@code NUM}, string literals {@code STR} and character
 *       literals {@code CHR}.</li>
 *   <li>Operators and punctuation are kept, with multi-character operators such as
 *       {@code ++} and {@code <=} as one token.</li>
 * </ul>
 */
public class CodeTokenizer {

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while",
            "true", "false", "null"));

    private static final String[] OPERATORS = {
            ">>>=", "<<=", ">>=", ">>>", "...", "->", "::", "++", "--", "+=", "-=", "*=", "/=",
            "%=", "&=", "|=", "^=", "==", "!=", "<=", ">=", "&&", "||", "<<", ">>"};

    /** @return the tokens of the given normalised code, in order */
    public List<String> tokenise(String normalisedCode) {
        if (normalisedCode == null) {
            throw new IllegalArgumentException("normalisedCode must not be null");
        }
        String s = normalisedCode;
        int n = s.length();
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isJavaIdentifierStart(c)) {
                int start = i++;
                while (i < n && Character.isJavaIdentifierPart(s.charAt(i))) {
                    i++;
                }
                String word = s.substring(start, i);
                tokens.add(KEYWORDS.contains(word) ? word : "ID");
            } else if (Character.isDigit(c) || (c == '.' && i + 1 < n && Character.isDigit(s.charAt(i + 1)))) {
                i = skipNumber(s, i);
                tokens.add("NUM");
            } else if (s.startsWith("\"\"\"", i)) {
                int end = s.indexOf("\"\"\"", i + 3);
                i = end < 0 ? n : end + 3;
                tokens.add("STR");
            } else if (c == '"' || c == '\'') {
                i = skipQuoted(s, i, c);
                tokens.add(c == '"' ? "STR" : "CHR");
            } else {
                String op = matchOperator(s, i);
                tokens.add(op);
                i += op.length();
            }
        }
        return tokens;
    }

    private static int skipNumber(String s, int i) {
        int n = s.length();
        boolean hex = s.startsWith("0x", i) || s.startsWith("0X", i);
        while (i < n) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                i++;
            } else if ((c == '+' || c == '-') && !hex && "eE".indexOf(s.charAt(i - 1)) >= 0) {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    private static int skipQuoted(String s, int i, char quote) {
        int n = s.length();
        i++;
        while (i < n && s.charAt(i) != quote && s.charAt(i) != '\n') {
            i += s.charAt(i) == '\\' ? 2 : 1;
        }
        return Math.min(n, i + 1);
    }

    private static String matchOperator(String s, int i) {
        for (String op : OPERATORS) {
            if (s.startsWith(op, i)) {
                return op;
            }
        }
        return String.valueOf(s.charAt(i));
    }
}
