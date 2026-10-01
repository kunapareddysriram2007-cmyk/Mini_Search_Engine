package com.minisearchengine.core.string;

/**
 * Levenshtein Edit Distance (Dynamic Programming) — Module 3 syllabus.
 *
 * Strict constraint: Zero java.util.* imports.
 *
 * ================================
 * DEFINITION
 * ================================
 * The Levenshtein distance between two strings is the minimum number of
 * single-character edits (insertions, deletions, substitutions) required
 * to transform one string into the other.
 *
 * Example: "kitten" → "sitting"
 *   kitten → sitten  (substitute k→s)
 *   sitten → sittin  (substitute e→i)
 *   sittin → sitting (insert g)
 *   distance = 3
 *
 * ================================
 * DP RECURRENCE
 * ================================
 *   dp[i][j] = edit distance between the first i chars of a and
 *              the first j chars of b.
 *
 *   dp[0][j] = j                (insert all j chars of b)
 *   dp[i][0] = i                (delete all i chars of a)
 *   dp[i][j] = dp[i-1][j-1]                       if a[i-1] == b[j-1]
 *            = 1 + min( dp[i-1][j],     (delete a[i-1])
 *                       dp[i][j-1],     (insert b[j-1])
 *                       dp[i-1][j-1] )  (substitute)
 *
 * ================================
 * SPACE OPTIMISATION
 * ================================
 * Row i of the table depends only on row i-1, so we keep just TWO rows
 * (previous and current) → O(min(m, n)) auxiliary space instead of O(m·n).
 * We always make the SHORTER string the inner dimension (columns).
 *
 * ================================
 * COMPLEXITY
 * ================================
 * distance(a, b):
 *   Time:  O(m·n)          — every cell of the conceptual table is filled once.
 *   Space: O(min(m, n))    — two rolling rows over the shorter string.
 *
 * withinThreshold(a, b, k):
 *   Time:  O(m·n) worst case, but EARLY-ABANDONS as soon as the minimum
 *          value of the current row exceeds k (the distance can only grow),
 *          and rejects immediately when the length difference alone exceeds k.
 *   Space: O(min(m, n)).
 *
 * Role in the search engine: FUZZY match mode — query terms are compared
 * against the index vocabulary; terms within a small threshold are treated
 * as spelling variants of the user's input.
 */
public class LevenshteinDistance {

    /** Substitution cost (1 = classic Levenshtein; could be weighted later). */
    private static final int SUBSTITUTE_COST = 1;
    /** Insertion cost. */
    private static final int INSERT_COST = 1;
    /** Deletion cost. */
    private static final int DELETE_COST = 1;

    /**
     * Computes the Levenshtein distance between a and b using two rolling
     * DP rows (O(min(m, n)) space).
     *
     * @param a first string (must not be null)
     * @param b second string (must not be null)
     * @return minimum number of single-character edits a → b (>= 0)
     */
    public int distance(String a, String b) {
        requireNonNull(a, "a");
        requireNonNull(b, "b");

        if (a.equals(b)) {
            return 0;
        }

        // Make b the SHORTER string so the DP rows are as small as possible.
        if (b.length() > a.length()) {
            String tmp = a;
            a = b;
            b = tmp;
        }

        int m = a.length();
        int n = b.length();

        if (n == 0) {
            return m; // delete every character of a
        }

        // prev[j] = dp[i-1][j], curr[j] = dp[i][j]
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        // Row 0: distance between "" and b[0..j] = j insertions
        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= m; i++) {
            curr[0] = i; // distance between a[0..i] and "" = i deletions
            char ca = a.charAt(i - 1);

            for (int j = 1; j <= n; j++) {
                if (ca == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1]; // characters match: no cost
                } else {
                    int delete = prev[j] + DELETE_COST;       // delete a[i-1]
                    int insert = curr[j - 1] + INSERT_COST;   // insert b[j-1]
                    int substitute = prev[j - 1] + SUBSTITUTE_COST;
                    curr[j] = min3(delete, insert, substitute);
                }
            }

            // Swap rows: current becomes previous for the next iteration.
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }

        return prev[n];
    }

    /**
     * Decides whether the edit distance between a and b is at most maxK,
     * WITHOUT necessarily computing the full distance.
     *
     * Early-abandon rules (both are sound lower bounds):
     *   1. |len(a) - len(b)| > maxK  → distance > maxK, answer false at O(1).
     *   2. If the minimum value in the current DP row exceeds maxK, the final
     *      distance can only be >= that minimum → abort with false.
     *
     * @param a     first string (must not be null)
     * @param b     second string (must not be null)
     * @param maxK  maximum allowed edit distance (>= 0)
     * @return true iff distance(a, b) <= maxK
     */
    public boolean withinThreshold(String a, String b, int maxK) {
        requireNonNull(a, "a");
        requireNonNull(b, "b");
        if (maxK < 0) {
            throw new IllegalArgumentException("maxK must be >= 0.");
        }

        // Rule 1: pure length difference is already a lower bound on edits.
        if (Math.abs(a.length() - b.length()) > maxK) {
            return false;
        }
        if (a.equals(b)) {
            return true;
        }

        // Make b the shorter string (rows sized by it).
        if (b.length() > a.length()) {
            String tmp = a;
            a = b;
            b = tmp;
        }

        int m = a.length();
        int n = b.length();
        if (n == 0) {
            return m <= maxK;
        }

        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];
        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            int rowMin = curr[0];

            for (int j = 1; j <= n; j++) {
                if (ca == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    int delete = prev[j] + DELETE_COST;
                    int insert = curr[j - 1] + INSERT_COST;
                    int substitute = prev[j - 1] + SUBSTITUTE_COST;
                    curr[j] = min3(delete, insert, substitute);
                }
                if (curr[j] < rowMin) {
                    rowMin = curr[j];
                }
            }

            // Rule 2: this row's minimum is a lower bound for the final distance.
            if (rowMin > maxK) {
                return false;
            }

            int[] swap = prev;
            prev = curr;
            curr = swap;
        }

        return prev[n] <= maxK;
    }

    // ------------------------------------------------------------------
    // helpers (java.lang only)
    // ------------------------------------------------------------------

    private static int min3(int x, int y, int z) {
        int m = x < y ? x : y;
        return m < z ? m : z;
    }

    private static void requireNonNull(String s, String name) {
        if (s == null) {
            throw new IllegalArgumentException(name + " must not be null.");
        }
    }
}
