package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Naive String Matching Algorithm.
 * 
 * Concept:
 * Slides the pattern across the text character-by-character from index 0 up to (n - m).
 * For each starting position i, it compares pattern[j] with text[i + j]. If all m characters
 * match, position i is recorded as a match.
 * 
 * Time Complexity:
 * - Worst-case: O((n - m + 1) * m), occurs on degenerate inputs like text = "AAAAAAA" and pattern = "AAA".
 * - Best-case: O(n), occurs when the first character of the pattern rarely matches the text.
 * 
 * Space Complexity:
 * - Auxiliary Space: O(1) beyond the output list storing match indices.
 * 
 * Constraints:
 * - Zero java.util.* imports.
 * - Zero String.indexOf() or regex library calls.
 */
public class NaiveStringMatcher implements StringMatcher {

    /**
     * Searches for all occurrences of pattern in text using the naive sliding-window approach.
     *
     * @param text    The source text string.
     * @param pattern The substring pattern to look for.
     * @return CustomArrayList<Integer> containing 0-based starting indices of matches.
     */
    @Override
    public CustomArrayList<Integer> search(String text, String pattern) {
        CustomArrayList<Integer> matches = new CustomArrayList<>();

        // Handle null inputs or invalid search criteria
        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        // An empty pattern does not constitute a valid character match
        if (m == 0 || m > n) {
            return matches;
        }

        // Slide the pattern across the text
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            // Compare characters one by one
            while (j < m && text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }
            // If the inner loop matched the entire pattern length
            if (j == m) {
                matches.add(i);
            }
        }

        return matches;
    }
}