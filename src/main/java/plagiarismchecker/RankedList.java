package plagiarismchecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A custom generic data structure: a list that keeps its items in order as they are added.
 *
 * <p>It is backed by an array that grows when full. To add an item, the position is found
 * with a binary search (O(log n) comparisons) and the later items are shifted along by one
 * place (O(n) moves). Items that compare as equal stay in the order they were added, so
 * the result is the same as a stable sort.</p>
 *
 * @param <T> the type of item held
 */
public class RankedList<T> implements Iterable<T> {

    private static final int INITIAL_CAPACITY = 8;

    private final Comparator<? super T> order;
    private Object[] items = new Object[INITIAL_CAPACITY];
    private int size;

    /** @param order decides which of two items comes first */
    public RankedList(Comparator<? super T> order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        this.order = order;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Adds an item in its place in the order. */
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item must not be null");
        }
        if (size == items.length) {
            items = Arrays.copyOf(items, items.length * 2);
        }
        int position = insertionPoint(item);
        System.arraycopy(items, position, items, position + 1, size - position);
        items[position] = item;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
        return (T) items[index];
    }

    /** The first {@code n} items (or fewer if the list is shorter), as a new list. */
    public List<T> top(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        List<T> out = new ArrayList<>();
        for (int i = 0; i < Math.min(n, size); i++) {
            out.add(get(i));
        }
        return out;
    }

    public List<T> toList() {
        return top(size);
    }

    /** Binary search for the first position after every item that sorts before or equal to the new one. */
    private int insertionPoint(T item) {
        int low = 0;
        int high = size;
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (order.compare(get(mid), item) <= 0) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int next;

            @Override
            public boolean hasNext() {
                return next < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(next++);
            }
        };
    }
}
