package com.minisearchengine.core.ds;

/**
 * Stable top-down Merge Sort built from scratch over {@link CustomArrayList}.
 *
 * Strict constraint: Zero java.util.* imports.
 *
 * =====================
 * WHY MERGE SORT HERE
 * =====================
 * The search engine needs a STABLE sort: when two documents have equal
 * relevance score, their relative order must be preserved (input order =
 * document id order), so results are deterministic across runs.
 * Randomized QuickSort (see {@link CustomSort}) is NOT stable; MergeSort is.
 *
 * ====================
 * ALGORITHM
 * ====================
 * 1. If the list has 0 or 1 elements, it is already sorted.
 * 2. Split the list into two halves.
 * 3. Recursively sort each half.
 * 4. Merge the two sorted halves, ALWAYS taking the left element on ties
 *    (the tie rule is exactly what makes the sort stable).
 *
 * ====================
 * COMPLEXITY
 * ====================
 * Time:  O(n log n) in ALL cases (best/average/worst) — log n levels,
 *        O(n) merge work per level.
 * Space: O(n) auxiliary — one Object[] buffer reused across merges
 *        plus O(log n) recursion stack.
 *
 * Used by the ranking pipeline (final ordering of search results) and
 * anywhere deterministic, stable ordering is required.
 */
public class MergeSort {

    private MergeSort() {
        // utility class — no instances
    }

    /**
     * Sorts the given list in place, ascending according to the comparator.
     * The sort is stable: elements that compare equal keep their original
     * relative order.
     *
     * @param list       the list to sort in place (must not be null)
     * @param comparator ordering; compare(a,b) <= 0 means a comes before b
     * @param <T>        element type
     */
    public static <T> void sort(CustomArrayList<T> list,
                                CustomPriorityQueue.CustomComparator<T> comparator) {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null.");
        }
        if (comparator == null) {
            throw new IllegalArgumentException("comparator must not be null.");
        }
        int n = list.size();
        if (n < 2) {
            return; // already sorted
        }

        // Copy elements into a working array; merges write results back.
        Object[] work = new Object[n];
        for (int i = 0; i < n; i++) {
            work[i] = list.get(i);
        }

        Object[] buffer = new Object[n];
        sortRange(work, buffer, 0, n - 1, comparator);

        for (int i = 0; i < n; i++) {
            @SuppressWarnings("unchecked")
            T element = (T) work[i];
            list.set(i, element);
        }
    }

    /** Recursive merge sort over the half-open-ish inclusive range [lo, hi]. */
    private static <T> void sortRange(Object[] work, Object[] buffer, int lo, int hi,
                                      CustomPriorityQueue.CustomComparator<T> comparator) {
        if (lo >= hi) {
            return; // base case: single element
        }
        int mid = (lo + hi) >>> 1;
        sortRange(work, buffer, lo, mid, comparator);
        sortRange(work, buffer, mid + 1, hi, comparator);
        merge(work, buffer, lo, mid, hi, comparator);
    }

    /**
     * Merges the two sorted runs work[lo..mid] and work[mid+1..hi] into
     * work[lo..hi]. STABILITY: on a tie, the element from the LEFT run
     * (earlier original position) is taken first.
     */
    @SuppressWarnings("unchecked")
    private static <T> void merge(Object[] work, Object[] buffer, int lo, int mid, int hi,
                                  CustomPriorityQueue.CustomComparator<T> comparator) {
        for (int i = lo; i <= hi; i++) {
            buffer[i] = work[i];
        }

        int left = lo;
        int right = mid + 1;
        int out = lo;

        while (left <= mid && right <= hi) {
            // <= 0 → left wins ties → stable
            if (comparator.compare((T) buffer[left], (T) buffer[right]) <= 0) {
                work[out++] = buffer[left++];
            } else {
                work[out++] = buffer[right++];
            }
        }
        while (left <= mid) {
            work[out++] = buffer[left++];
        }
        while (right <= hi) {
            work[out++] = buffer[right++];
        }
    }
}
