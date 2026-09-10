package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Interface for multi-pattern matching algorithms (e.g. Aho-Corasick).
 */
public interface MultiPatternMatcher {
    /**
     * Represents an occurrence of a specific pattern in the text.
     */
    class Match {
        private final int startIndex;
        private final String pattern;

        public Match(int startIndex, String pattern) {
            this.startIndex = startIndex;
            this.pattern = pattern;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public String getPattern() {
            return pattern;
        }
    }

    /**
     * Searches for occurrences of multiple patterns in the given text simultaneously.
     *
     * @param text The text to scan.
     * @return A list of Match objects.
     */
    CustomArrayList<Match> searchAll(String text);
}