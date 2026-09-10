package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Rabin-Karp String Matching Algorithm using a Rolling Hash.
 *
 * ========================
 * CORE IDEA: ROLLING HASH
 * ========================
 * Instead of comparing every character at each window position (Naive: O(nm)),
 * Rabin-Karp computes a hash for the pattern and for each window of the text.
 *
 * The key insight is the ROLLING HASH: when sliding the window one position
 * to the right, we can update the hash in O(1) using the formula:
 *
 *   newHash = (base * (oldHash - text[i] * h) + text[i + m]) % mod
 *
 * where h = base^(m-1) % mod  is precomputed.
 *
 * A hash match alone may be a FALSE POSITIVE (hash collision).
 * After a hash match, every character is verified explicitly.
 * This makes correctness unconditional.
 *
 * ========================
 * HASH DESIGN
 * ========================
 * - BASE  = 31  (prime, standard for lowercase Latin and ASCII)
 * - MOD   = 1_000_000_007  (large prime to minimise collisions)
 * - Arithmetic uses long to prevent int overflow during intermediate steps.
 * - Rolling subtraction uses +MOD before % to keep results non-negative.
 *
 * ========================
 * COMPLEXITY
 * ========================
 * Preprocessing:
 *   O(m)  — compute pattern hash and h = base^(m-1).
 *
 * Search:
 *   Average O(n + m)  — O(n) hash comparisons + O(m) character verification
 *                        amortised over true matches only.
 *   Worst-case O(nm)  — if every window is a false positive (contrived input).
 *
 * Space:
 *   O(1) auxiliary beyond the result list.
 *
 * Constraints:
 *   Zero java.util.* imports.
 *   Zero String.indexOf() or regex.
 *   Explicit character verification after every hash match.
 */
public class RabinKarpMatcher implements StringMatcher {

    private static final long BASE = 31L;
    private static final long MOD  = 1_000_000_007L;

    /**
     * Searches for all occurrences of pattern in text using Rabin-Karp rolling hash.
     *
     * @param text    Source text.
     * @param pattern Pattern to find.
     * @return CustomArrayList<Integer> of 0-based start indices of verified matches.
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

        // Precompute h = BASE^(m-1) % MOD — needed to remove the leading character
        long h = 1L;
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % MOD;
        }

        // Compute hash of pattern and first window of text
        long patternHash = 0L;
        long windowHash  = 0L;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + pattern.charAt(i)) % MOD;
            windowHash  = (windowHash  * BASE + text.charAt(i))    % MOD;
        }

        // Slide window across text
        for (int i = 0; i <= n - m; i++) {
            if (patternHash == windowHash) {
                // Hash match: verify character-by-character (guard against false positives)
                if (verifyMatch(text, i, pattern)) {
                    matches.add(i);
                }
            }

            // Roll the hash: remove leading char, add next char
            if (i < n - m) {
                windowHash = (BASE * (windowHash - (long) text.charAt(i) * h % MOD + MOD) % MOD
                              + text.charAt(i + m)) % MOD;
            }
        }

        return matches;
    }

    /**
     * Verifies a potential match character-by-character.
     * Called only after a hash match to eliminate false positives.
     *
     * @param text    Source text.
     * @param start   Starting index in text.
     * @param pattern Pattern to verify against.
     * @return true if text[start .. start+m-1] exactly equals pattern.
     */
    private boolean verifyMatch(String text, int start, String pattern) {
        int m = pattern.length();
        for (int j = 0; j < m; j++) {
            if (text.charAt(start + j) != pattern.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}