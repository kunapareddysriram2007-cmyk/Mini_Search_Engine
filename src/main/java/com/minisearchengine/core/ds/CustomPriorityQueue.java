package com.minisearchengine.core.ds;

/**
 * Custom Min-Heap / Max-Heap Priority Queue built from scratch.
 * 
 * Strict constraint: Zero java.util.* imports.
 * 
 * Design & Mechanics:
 * - Backed by an internal 0-indexed Object[] array.
 * - Supports CustomComparator<T> or Comparable natural ordering.
 * - Parent: (i - 1) / 2
 * - Left child: 2 * i + 1
 * - Right child: 2 * i + 2
 * 
 * Time Complexities:
 * - Offer (insert): O(log n)
 * - Poll (extract min/max): O(log n)
 * - Peek: O(1)
 * 
 * Space Complexity:
 * - O(n)
 * 
 * @param <T> Element type
 */
public class CustomPriorityQueue<T> {

    @FunctionalInterface
    public interface CustomComparator<E> {
        int compare(E a, E b);
    }

    private static final int DEFAULT_CAPACITY = 11;
    private Object[] queue;
    private int size = 0;
    private final CustomComparator<T> comparator;

    public CustomPriorityQueue() {
        this(DEFAULT_CAPACITY, null);
    }

    public CustomPriorityQueue(CustomComparator<T> comparator) {
        this(DEFAULT_CAPACITY, comparator);
    }

    public CustomPriorityQueue(int initialCapacity, CustomComparator<T> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Capacity must be >= 1");
        }
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void offer(T element) {
        if (element == null) {
            throw new NullPointerException("Null elements not supported");
        }
        if (size >= queue.length) {
            grow();
        }
        siftUp(size++, element);
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        return size == 0 ? null : (T) queue[0];
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        if (size == 0) return null;
        T result = (T) queue[0];
        int s = --size;
        T x = (T) queue[s];
        queue[s] = null;
        if (s != 0) {
            siftDown(0, x);
        }
        return result;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            queue[i] = null;
        }
        size = 0;
    }

    private void grow() {
        int oldCap = queue.length;
        int newCap = oldCap + (oldCap < 64 ? oldCap + 2 : (oldCap >> 1));
        Object[] newQueue = new Object[newCap];
        for (int i = 0; i < size; i++) {
            newQueue[i] = queue[i];
        }
        this.queue = newQueue;
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int k, T x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (compare(x, (T) e) >= 0) {
                break;
            }
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int k, T x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            Object c = queue[child];
            int right = child + 1;
            if (right < size && compare((T) c, (T) queue[right]) > 0) {
                child = right;
                c = queue[child];
            }
            if (compare(x, (T) c) <= 0) {
                break;
            }
            queue[k] = c;
            k = child;
        }
        queue[k] = x;
    }

    @SuppressWarnings("unchecked")
    private int compare(T a, T b) {
        if (comparator != null) {
            return comparator.compare(a, b);
        }
        return ((Comparable<? super T>) a).compareTo(b);
    }
}