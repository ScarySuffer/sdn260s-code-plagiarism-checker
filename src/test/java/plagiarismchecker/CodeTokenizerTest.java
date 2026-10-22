package plagiarismchecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class CodeTokenizerTest {

    private final CodeTokenizer tokenizer = new CodeTokenizer();

    @Test
    void keepsKeywordsAndReplacesIdentifiers() {
        assertEquals(Arrays.asList("int", "ID", "=", "NUM", ";"), tokenizer.tokenise("int total = 0;"));
    }

    @Test
    void renamedVariablesGiveTheSameTokens() {
        assertEquals(tokenizer.tokenise("int total = 0; total += i;"),
                tokenizer.tokenise("int result=0;result+=n;"));
    }

    @Test
    void treatsMultiCharacterOperatorsAsOneToken() {
        assertEquals(Arrays.asList("ID", "++", ";", "ID", "<=", "ID", "&&", "ID", "!=", "ID"),
                tokenizer.tokenise("i++; a<=b && c!=d"));
    }

    @Test
    void abstractsLiterals() {
        assertEquals(Arrays.asList("ID", "=", "STR", ";", "ID", "=", "CHR", ";", "ID", "=", "NUM", ";"),
                tokenizer.tokenise("s = \"he said \\\"hi\\\"\"; c = 'x'; d = 3.5e-2;"));
    }

    @Test
    void handlesMethodCallsAndArrays() {
        assertEquals(Arrays.asList("ID", ".", "ID", "(", "ID", "[", "NUM", "]", ")", ";"),
                tokenizer.tokenise("out.println(arr[0]);"));
    }

    @Test
    void emptyInputGivesNoTokens() {
        assertEquals(Collections.emptyList(), tokenizer.tokenise(""));
    }

    @Test
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> tokenizer.tokenise(null));
    }
}
