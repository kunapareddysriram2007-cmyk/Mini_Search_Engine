package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Interface for exact pattern matching algorithms.
 */
public interface StringMatcher {
    /**
     * Finds all starting indices where the pattern occurs in the text.
     *
     * @param text    The input document or corpus text.
     * @param pattern The pattern to search for.
     * @return A CustomArrayList containing 0-based start indices.
     */
    CustomArrayList<Integer> search(String text, String pattern);
}