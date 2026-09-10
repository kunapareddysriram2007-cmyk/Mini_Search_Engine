package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Z Algorithm for exact string pattern matching.
 *
 * =====================
 * CORE IDEA: THE Z-ARRAY
 * =====================
 * For a string S of length N, the Z-array Z[] is defined such that:
 *   Z[i] = length of the longest substring starting at S[i] that is also
 *           a PREFIX of S.
 *   Z[0] is conventionally set to 0 (the entire string is trivially a prefix).
 *
 * Example: S = "AABXAA"
 *   Z[0] = 0  (convention)
 *   Z[1] = 1  ("A" matches prefix "A")
 *   Z[2] = 0  ("B" does not match prefix "A")
 *   Z[3] = 0  ("X" does not match)
 *   Z[4] = 2  ("AA" matches prefix "AA")
 *   Z[5] = 1  ("A" matches prefix "A")
 *
 * ========================
 * HOW SEARCHING USES Z
 * ========================
 * To search for pattern P (length m) in text T (length n):
 *   1. Build a concatenated string S = P + "$" + T  (sentinel "$" never appears in P or T).
 *   2. Compute the Z-array for S.
 *   3. For each position i >= m + 1 in Z[], if Z[i] == m then a match starts
 *      at position (i - m - 1) in the original text T.
 *
 * The sentinel ensures Z-values in the text portion never exceed m.
 *
 * ========================
 * EFFICIENT Z-ARRAY BUILD
 * ========================
 * The Z-array is built in O(n+m) total by maintaining a "Z-box" [L, R]:
 *   L and R are the left and right boundaries of the rightmost Z-box seen so far.
 *   A Z-box at position k is the interval [k, k + Z[k] - 1].
 *
 *   For each i > 0:
 *   - If i > R:  expand naively from S[i], update L = i, R = i + Z[i] - 1.
 *   - If i <= R: Z[i] = min(R - i + 1, Z[i - L]).
 *                Then expand beyond R if possible, updating L, R.
 *
 * This amortised trick ensures each character is compared at most twice.
 *
 * =====================
 * COMPLEXITY
 * =====================
 * Time (buildZ):
 *   O(n + m) — linear pass with Z-box amortisation.
 * Time (search):
 *   O(n + m) — O(n + m) for buildZ + O(n + m) scan for matches.
 * Space:
 *   O(n + m) — concatenated string + Z-array.
 *
 * Constraints:
 *   Zero java.util.* imports.
 *   Zero String.indexOf() or regex.
 */
public class ZAlgorithmMatcher implements StringMatcher {

    /**
     * Searches for all occurrences of pattern in text using the Z algorithm.
     *
     * @param text    Source text.
     * @param pattern Pattern to find.
     * @return CustomArrayList<Integer> of 0-based start indices in text.
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

        // Build concatenated string: pattern + sentinel + text
        // Use char[] to avoid any library string builder dependency
        char[] concat = new char[m + 1 + n];
        for (int i = 0; i < m; i++)        concat[i]         = pattern.charAt(i);
        concat[m] = '$';                    // sentinel never in alphabet
        for (int i = 0; i < n; i++)        concat[m + 1 + i] = text.charAt(i);

        int[] z = buildZ(concat);

        // Positions where Z[i] == m correspond to pattern matches in text
        for (int i = m + 1; i < concat.length; i++) {
            if (z[i] == m) {
                // Text match starts at (i - m - 1)
                matches.add(i - m - 1);
            }
        }

        return matches;
    }

    /**
     * Builds the Z-array for the given character array in O(length) time.
     *
     * Z[0] = 0 by convention.
     * For i > 0: Z[i] = length of the longest prefix of s[] that matches s[i..].
     *
     * @param s Character array (concatenated string).
     * @return int[] Z-array of same length as s.
     */
    public int[] buildZ(char[] s) {
        int len = s.length;
        int[] z = new int[len];
        z[0] = 0; // convention

        int l = 0; // left boundary of current rightmost Z-box
        int r = 0; // right boundary (exclusive) of current rightmost Z-box

        for (int i = 1; i < len; i++) {
            if (i < r) {
                // i is inside the current Z-box; use cached value
                z[i] = Math.min(r - i, z[i - l]);
            }
            // Try to extend beyond r (or start fresh if i >= r)
            while (i + z[i] < len && s[z[i]] == s[i + z[i]]) {
                z[i]++;
            }
            // Update Z-box if we extended beyond r
            if (i + z[i] > r) {
                l = i;
                r = i + z[i];
            }
        }

        return z;
    }
}