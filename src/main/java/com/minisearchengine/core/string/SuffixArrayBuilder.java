package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;

/**
 * Suffix Array construction using the Prefix-Doubling (Manber & Myers) algorithm,
 * combined with Kasai's algorithm for LCP array computation.
 *
 * Implements the {@link SuffixStructure} interface.
 *
 * ===============================
 * PART 1 — SUFFIX ARRAY
 * ===============================
 *
 * A Suffix Array SA[] for a string S of length n is a permutation of [0, n-1]
 * such that SA[i] is the starting index of the i-th lexicographically smallest
 * suffix of S.
 *
 * Example: S = "banana"
 *   Suffixes sorted lexicographically:
 *     "a"       -> index 5
 *     "ana"     -> index 3
 *     "anana"   -> index 1
 *     "banana"  -> index 0
 *     "na"      -> index 4
 *     "nana"    -> index 2
 *   SA = [5, 3, 1, 0, 4, 2]
 *
 * ---------------------------------
 * ALGORITHM: Prefix Doubling
 * ---------------------------------
 * Sorts suffixes by progressively doubling the key length:
 *   Round 0: rank[i] = char code of S[i]           (single char key)
 *   Round k: key for suffix i = (rank[i], rank[i + 2^k])  (double the window)
 *
 * Uses a stable sort (merge sort on int[] pairs) in each round.
 * After O(log n) rounds, all suffixes are fully distinguished.
 *
 * Time:  O(n log^2 n)  — O(log n) rounds, each using O(n log n) merge sort.
 * Space: O(n)          — rank[], tmp[], SA[], auxiliary merge buffer.
 *
 * ===============================
 * PART 2 — KASAI LCP ARRAY
 * ===============================
 *
 * LCP[i] = length of the longest common prefix between SA[i] and SA[i-1].
 * LCP[0] = 0 by convention.
 *
 * Kasai's Algorithm exploits the following property:
 *   If LCP(SA[i], SA[i-1]) = k, then LCP(SA[rank[SA[i]+1]], SA[rank[SA[i]+1]-1]) >= k-1.
 *
 * This means we compute LCP values in suffix-order (not SA-order), carrying over
 * the previous LCP length minus 1 as a guaranteed lower bound.
 *
 * Time:  O(n) — each character is examined at most twice across all LCP computations.
 * Space: O(n) — for rank[] (inverse SA) and LCP[].
 *
 * ===============================
 * BINARY SEARCH (Pattern Search)
 * ===============================
 * Uses the sorted SA to binary-search for all occurrences of a pattern.
 * Compares the pattern against the suffix starting at SA[mid].
 *
 * Time:  O(m log n + z)  where m = pattern length, z = number of matches.
 * Space: O(1) auxiliary.
 *
 * Constraints:
 *   Zero java.util.* imports.
 *   No library sort. Uses a private stable merge sort on int[] pairs.
 */
public class SuffixArrayBuilder implements SuffixStructure {

    private final String text;
    private final int n;
    private final int[] sa;    // suffix array
    private final int[] lcp;   // LCP array (Kasai)

    /**
     * Builds the Suffix Array and LCP array for the given text.
     *
     * @param text The input string (must not be null).
     */
    public SuffixArrayBuilder(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Input text must not be null.");
        }
        this.text = text;
        this.n    = text.length();
        this.sa   = buildSuffixArray();
        this.lcp  = buildLCP();
    }

    // =========================================================
    // Phase 1 — Suffix Array (Prefix Doubling)
    // =========================================================

    private int[] buildSuffixArray() {
        if (n == 0) return new int[0];
        if (n == 1) return new int[]{0};

        int[] sa   = new int[n];
        int[] rank = new int[n];
        int[] tmp  = new int[n];

        // Initialise SA = [0, 1, 2, ..., n-1] and rank[i] = char code of text[i]
        for (int i = 0; i < n; i++) {
            sa[i]   = i;
            rank[i] = text.charAt(i);
        }

        // Prefix-doubling rounds: gap doubles each iteration (1, 2, 4, 8, ...)
        for (int gap = 1; gap < n; gap <<= 1) {
            final int[] r = rank;
            final int   g = gap;

            // Sort SA by (rank[i], rank[i+gap]) using stable merge sort
            mergeSortByRank(sa, 0, n - 1, r, g, tmp);

            // Recompute ranks after this round
            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]];
                // Advance rank if this pair differs from the previous
                if (r[sa[i]] != r[sa[i - 1]]
                        || secondRank(sa[i], g, n, r) != secondRank(sa[i - 1], g, n, r)) {
                    tmp[sa[i]]++;
                }
            }

            // Copy new ranks back
            for (int i = 0; i < n; i++) rank[i] = tmp[i];

            // If all ranks are distinct, the suffix array is complete
            if (rank[sa[n - 1]] == n - 1) break;
        }

        return sa;
    }

    /** Returns rank[i + gap] if in bounds, else -1 (sentinel for end-of-string). */
    private static int secondRank(int i, int gap, int n, int[] rank) {
        int j = i + gap;
        return j < n ? rank[j] : -1;
    }

    // =========================================================
    // Stable Merge Sort on SA[] by (rank[i], rank[i+gap])
    // =========================================================
    // Operates entirely on int[] — no library sort, no CustomArrayList allocation.

    private void mergeSortByRank(int[] arr, int lo, int hi,
                                 int[] rank, int gap, int[] buf) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        mergeSortByRank(arr, lo, mid,      rank, gap, buf);
        mergeSortByRank(arr, mid + 1, hi,  rank, gap, buf);
        merge(arr, lo, mid, hi, rank, gap, buf);
    }

    private void merge(int[] arr, int lo, int mid, int hi,
                       int[] rank, int gap, int[] buf) {
        // Copy segment into buf
        for (int i = lo; i <= hi; i++) buf[i] = arr[i];

        int left  = lo;
        int right = mid + 1;
        int k     = lo;

        while (left <= mid && right <= hi) {
            if (compare(buf[left], buf[right], rank, gap) <= 0) {
                arr[k++] = buf[left++];
            } else {
                arr[k++] = buf[right++];
            }
        }
        while (left  <= mid) arr[k++] = buf[left++];
        while (right <= hi)  arr[k++] = buf[right++];
    }

    private int compare(int a, int b, int[] rank, int gap) {
        if (rank[a] != rank[b]) return Integer.compare(rank[a], rank[b]);
        return Integer.compare(secondRank(a, gap, n, rank),
                               secondRank(b, gap, n, rank));
    }

    // =========================================================
    // Phase 2 — Kasai LCP Array
    // =========================================================

    private int[] buildLCP() {
        if (n == 0) return new int[0];
        if (n == 1) return new int[]{0};

        int[] lcp  = new int[n];
        int[] rank = new int[n]; // inverse SA: rank[sa[i]] = i

        for (int i = 0; i < n; i++) rank[sa[i]] = i;

        int k = 0; // carry-over LCP length (the k-1 optimisation)

        for (int i = 0; i < n; i++) {
            if (rank[i] == 0) {
                // First suffix in sorted order has no predecessor; reset k
                k = 0;
                continue;
            }

            int j = sa[rank[i] - 1]; // preceding suffix in SA order

            // Extend LCP character by character from position k
            while (i + k < n && j + k < n && text.charAt(i + k) == text.charAt(j + k)) {
                k++;
            }

            lcp[rank[i]] = k;

            // Key optimisation: LCP for the next suffix is at least k-1
            if (k > 0) k--;
        }

        return lcp;
    }

    // =========================================================
    // Public API
    // =========================================================

    @Override
    public int[] getSuffixArray() {
        return sa;
    }

    @Override
    public int[] getLcpArray() {
        return lcp;
    }

    /**
     * Finds all positions where pattern occurs in text using binary search on SA.
     *
     * Binary searches for the leftmost and rightmost positions where the suffix
     * starts with the pattern, then collects all indices in between.
     *
     * Time: O(m log n + z)
     */
    @Override
    public CustomArrayList<Integer> search(String pattern) {
        CustomArrayList<Integer> result = new CustomArrayList<>();
        if (pattern == null || pattern.isEmpty() || n == 0) return result;

        int m   = pattern.length();
        int lo  = lowerBound(pattern, m);
        int hi  = upperBound(pattern, m);

        for (int i = lo; i <= hi; i++) {
            result.add(sa[i]);
        }
        return result;
    }

    /** First SA index whose suffix has pattern as a prefix (or n if none). */
    private int lowerBound(String pattern, int m) {
        int lo = 0, hi = n;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (comparePrefix(sa[mid], pattern, m) < 0) lo = mid + 1;
            else hi = mid;
        }
        return lo;
    }

    /** One past the last SA index whose suffix has pattern as a prefix. */
    private int upperBound(String pattern, int m) {
        int lo = 0, hi = n;
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (comparePrefix(sa[mid], pattern, m) <= 0) lo = mid + 1;
            else hi = mid;
        }
        return lo - 1;
    }

    /**
     * Compares the first m characters of text[suffixStart..] against pattern.
     * Returns negative if suffix < pattern, 0 if suffix starts with pattern,
     * positive if suffix > pattern.
     */
    private int comparePrefix(int suffixStart, String pattern, int m) {
        for (int i = 0; i < m; i++) {
            if (suffixStart + i >= n) return -1; // suffix shorter than pattern
            char sc = text.charAt(suffixStart + i);
            char pc = pattern.charAt(i);
            if (sc != pc) return Character.compare(sc, pc);
        }
        return 0; // suffix starts with pattern
    }
}