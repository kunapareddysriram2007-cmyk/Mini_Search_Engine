package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Knuth-Morris-Pratt (KMP) String Matching Algorithm.
 *
 * ===========================
 * THE LPS ARRAY (KEY INSIGHT)
 * ===========================
 * LPS stands for "Longest Proper Prefix that is also a Suffix."
 *
 * For any prefix of the pattern (pattern[0..i]), lps[i] stores the length
 * of the longest proper prefix of that prefix which is also a suffix of it.
 *
 * Example: pattern = "ABABAB"
 *   lps[0] = 0  (no proper prefix for "A")
 *   lps[1] = 0  ("AB" -> no proper prefix = suffix)
 *   lps[2] = 1  ("ABA" -> "A" is both prefix and suffix)
 *   lps[3] = 2  ("ABAB" -> "AB" is both prefix and suffix)
 *   lps[4] = 3  ("ABABA" -> "ABA" is both prefix and suffix)
 *   lps[5] = 4  ("ABABAB" -> "ABAB" is both prefix and suffix)
 *
 * Why this helps in searching:
 * When a mismatch occurs at position j in the pattern (after matching j chars),
 * instead of resetting j to 0 and i back one step (Naive), KMP uses lps[j-1]
 * to skip the part of the pattern we already know matches. This prevents
 * redundant character comparisons.
 *
 * =====================
 * TIME & SPACE ANALYSIS
 * =====================
 * Preprocessing (buildLPS):
 *   - Time Complexity:  O(m) — each character is added at most once and
 *                               removed at most once from the "length" pointer.
 *   - Space Complexity: O(m) — for the lps[] array of size m.
 *
 * Searching (search):
 *   - Time Complexity:  O(n) — the text pointer i never moves backward;
 *                               the pattern pointer j is bounded by lps, so
 *                               total work across all mismatches is O(n).
 *   - Space Complexity: O(1) auxiliary (beyond the lps array).
 *
 * Overall:
 *   - Time:  O(n + m)
 *   - Space: O(m) for the LPS table.
 *
 * Constraints:
 * - Zero java.util.* imports.
 * - Zero String.indexOf() or regex.
 */
public class KMPStringMatcher implements StringMatcher {

    /**
     * Searches for all occurrences of pattern in text using the KMP algorithm.
     *
     * Step 1: Build the LPS array from the pattern (O(m)).
     * Step 2: Slide through the text using the LPS array to skip redundant
     *         comparisons (O(n)).
     *
     * @param text    The source text.
     * @param pattern The pattern to find.
     * @return CustomArrayList<Integer> of 0-based start indices of all matches.
     */
    @Override
    public CustomArrayList<Integer> search(String text, String pattern) {
        CustomArrayList<Integer> matches = new CustomArrayList<>();

        if (text == null || pattern == null) {
            return matches;
        }

        int n = text.length();
        int m = pattern.length();

        if (m == 0 || m > n) {
            return matches;
        }

        int[] lps = buildLPS(pattern);

        int i = 0; // index for text
        int j = 0; // index for pattern

        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }

            if (j == m) {
                // Full match found; match starts at (i - j)
                matches.add(i - j);
                // Use lps to continue searching for overlapping matches
                j = lps[j - 1];
            } else if (i < n && text.charAt(i) != pattern.charAt(j)) {
                if (j != 0) {
                    // Don't move i; shift j using LPS
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return matches;
    }

    /**
     * Builds the LPS (Longest Proper Prefix that is also a Suffix) array.
     *
     * Algorithm:
     *   - len  = length of the previous longest prefix-suffix (starts at 0).
     *   - i    = scans the pattern from position 1 onward.
     *   - lps[0] is always 0 by definition (no proper prefix for a 1-char string).
     *
     * If pattern[i] == pattern[len], we extend the current matching prefix:
     *     lps[i] = len + 1; i++; len++
     *
     * If there is a mismatch and len > 0, we fall back using lps itself:
     *     len = lps[len - 1]   (reuse earlier computed values, no character is re-read)
     *
     * If there is a mismatch and len == 0:
     *     lps[i] = 0; i++
     *
     * @param pattern The pattern string.
     * @return int[] lps array of length m.
     */
    public int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];

        lps[0] = 0; // Always 0 by definition
        int len = 0; // Length of previous longest prefix-suffix
        int i = 1;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    // Fall back using lps — no increment of i
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }
}