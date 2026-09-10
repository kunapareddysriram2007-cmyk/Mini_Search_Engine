package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Interface for suffix-based indexing structures (Suffix Array & LCP).
 */
public interface SuffixStructure {
    /**
     * Returns the computed Suffix Array (0-based starting indices of sorted suffixes).
     */
    int[] getSuffixArray();

    /**
     * Returns the computed Kasai LCP (Longest Common Prefix) array.
     */
    int[] getLcpArray();

    /**
     * Finds all occurrences of the query pattern using binary search on the suffix array.
     *
     * @param pattern Pattern to search.
     * @return List of 0-based match indices in the original text.
     */
    CustomArrayList<Integer> search(String pattern);
}