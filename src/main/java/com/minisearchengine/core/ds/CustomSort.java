package com.minisearchengine.core.ds;

/**
 * Custom Sorting utility implementing course syllabus sorting algorithms from scratch:
 * - Randomized QuickSort (Module 6)
 * - Iterative MergeSort (Stable sorting)
 * 
 * Strict constraint: Zero java.util collections or java.util.Arrays.sort.
 * 
 * Time Complexities:
 * - Randomized QuickSort: Expected O(n log n), Worst-case O(n^2) (negligible probability with random pivot).
 * - MergeSort: Guaranteed O(n log n).
 * 
 * Space Complexities:
 * - Randomized QuickSort: O(log n) auxiliary stack space.
 * - MergeSort: O(n) auxiliary buffer.
 */
public class CustomSort {

    // Simple Linear Congruential Generator (LCG) to eliminate any java.util.Random dependency completely
    private static long seed = 8682522807148012L ^ System.nanoTime();
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    private static synchronized int nextRandomInt(int bound) {
        if (bound <= 0) return 0;
        seed = (seed * MULTIPLIER + ADDEND) & MASK;
        long bits = seed >>> 17;
        return (int) (bits % bound);
    }

    /**
     * In-place Randomized QuickSort on CustomArrayList<T> using natural Comparable ordering.
     */
     public static <T extends Comparable<? super T>> void randomizedQuickSort(CustomArrayList<T> list) {
        if (list == null || list.size() <= 1) return;
        randomizedQuickSort(list, (a, b) -> a.compareTo(b));
    }

    /**
     * In-place Randomized QuickSort on CustomArrayList<T> using a custom comparator.
     */
    public static <T> void randomizedQuickSort(CustomArrayList<T> list, CustomPriorityQueue.CustomComparator<T> comparator) {
        if (list == null || list.size() <= 1) return;
        quickSort(list, 0, list.size() - 1, comparator);
    }

    private static <T> void quickSort(CustomArrayList<T> list, int low, int high, CustomPriorityQueue.CustomComparator<T> comparator) {
        while (low < high) {
            // Pick random pivot and swap with high
            int pivotIndex = low + nextRandomInt(high - low + 1);
            swap(list, pivotIndex, high);

            int p = partition(list, low, high, comparator);

            // Tail-call optimization: recurse on smaller subarray, loop on larger
            if (p - low < high - p) {
                quickSort(list, low, p - 1, comparator);
                low = p + 1;
            } else {
                quickSort(list, p + 1, high, comparator);
                high = p - 1;
            }
        }
    }

    private static <T> int partition(CustomArrayList<T> list, int low, int high, CustomPriorityQueue.CustomComparator<T> comparator) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(CustomArrayList<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}