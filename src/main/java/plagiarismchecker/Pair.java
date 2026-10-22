package plagiarismchecker;

import java.util.Objects;

/**
 * A small generic class that holds two values of possibly different types.
 *
 * <p>Used to describe a pair of files by their positions in the file list, so each pair
 * can be handed to a worker thread as one object.</p>
 *
 * @param <A> type of the first value
 * @param <B> type of the second value
 */
public final class Pair<A, B> {

    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /** Generic factory method; the compiler works out A and B from the arguments. */
    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?, ?> that = (Pair<?, ?>) other;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
