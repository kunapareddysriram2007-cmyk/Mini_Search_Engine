package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SuffixArrayBuilderTest {

    // =========================================================
    // Helper: verify the SA is actually lexicographically sorted
    // =========================================================
    private void assertLexicographicOrder(String text, int[] sa) {
        for (int i = 0; i + 1 < sa.length; i++) {
            String si = text.substring(sa[i]);
            String sj = text.substring(sa[i + 1]);
            assertTrue(si.compareTo(sj) <= 0,
                    "SA not sorted at positions " + i + " and " + (i + 1)
                    + ": \"" + si + "\" vs \"" + sj + "\"");
        }
    }

    // Helper: brute-force SA by sorting suffixes lexicographically
    private int[] bruteForceSA(String text) {
        int n = text.length();
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        // simple insertion sort on suffix strings (fine for small test inputs)
        for (int i = 1; i < n; i++) {
            int key = idx[i];
            int j = i - 1;
            while (j >= 0 && text.substring(idx[j]).compareTo(text.substring(key)) > 0) {
                idx[j + 1] = idx[j];
                j--;
            }
            idx[j + 1] = key;
        }
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = idx[i];
        return result;
    }

    // Helper: brute-force LCP
    private int[] bruteForceLCP(String text, int[] sa) {
        int n = sa.length;
        int[] lcp = new int[n];
        lcp[0] = 0;
        for (int i = 1; i < n; i++) {
            String a = text.substring(sa[i - 1]);
            String b = text.substring(sa[i]);
            int k = 0;
            while (k < a.length() && k < b.length() && a.charAt(k) == b.charAt(k)) k++;
            lcp[i] = k;
        }
        return lcp;
    }

    // =========================================================
    // Section 1: Suffix Array — Classic Examples
    // =========================================================

    @Test
    public void testBanana() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        int[] sa = sab.getSuffixArray();
        // Expected SA = [5, 3, 1, 0, 4, 2]
        assertArrayEquals(new int[]{5, 3, 1, 0, 4, 2}, sa);
    }

    @Test
    public void testMississippi() {
        String text = "mississippi";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    @Test
    public void testAbracadabra() {
        String text = "abracadabra";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    // =========================================================
    // Section 2: Edge Cases for Suffix Array
    // =========================================================

    @Test
    public void testSingleChar() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("a");
        assertArrayEquals(new int[]{0}, sab.getSuffixArray());
    }

    @Test
    public void testEmptyString() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("");
        assertArrayEquals(new int[]{}, sab.getSuffixArray());
    }

    @Test
    public void testAllSameChars() {
        // "aaaa": suffixes sorted = "a","aa","aaa","aaaa" -> SA = [3,2,1,0]
        SuffixArrayBuilder sab = new SuffixArrayBuilder("aaaa");
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder("aaaa", sa);
        assertArrayEquals(new int[]{3, 2, 1, 0}, sa);
    }

    @Test
    public void testTwoDistinctChars() {
        String text = "aab";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    @Test
    public void testAlreadySortedSuffixes() {
        // "abcd": suffixes "d","cd","bcd","abcd" -> sorted = "abcd","bcd","cd","d"
        String text = "abcd";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    @Test
    public void testReverseSortedChars() {
        // "dcba": suffixes sorted = "a","ba","cba","dcba"
        String text = "dcba";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    @Test
    public void testRepeatedPattern() {
        String text = "ababab";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        assertLexicographicOrder(text, sa);
        assertArrayEquals(bruteForceSA(text), sa);
    }

    @Test
    public void testTwoCharsOnly() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("ab");
        assertArrayEquals(new int[]{0, 1}, sab.getSuffixArray());
    }

    @Test
    public void testSAHasCorrectLength() {
        String text = "algorithm";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        assertEquals(text.length(), sab.getSuffixArray().length);
    }

    @Test
    public void testSAContainsAllIndices() {
        // Every index 0..n-1 must appear exactly once
        String text = "textbook";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] sa = sab.getSuffixArray();
        boolean[] seen = new boolean[text.length()];
        for (int v : sa) {
            assertFalse(seen[v], "Duplicate index " + v + " in SA");
            seen[v] = true;
        }
        for (boolean b : seen) assertTrue(b, "Missing index in SA");
    }

    // =========================================================
    // Section 3: Kasai LCP Array Correctness
    // =========================================================

    @Test
    public void testLCPBanana() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        // SA = [5,3,1,0,4,2]
        // suffixes:  "a","ana","anana","banana","na","nana"
        // LCP[0]=0, LCP[1]=1, LCP[2]=3, LCP[3]=0, LCP[4]=0, LCP[5]=2
        assertArrayEquals(new int[]{0, 1, 3, 0, 0, 2}, sab.getLcpArray());
    }

    @Test
    public void testLCPAllSame() {
        // "aaaa": SA=[3,2,1,0], suffixes="a","aa","aaa","aaaa"
        // LCP[0]=0, LCP[1]=1, LCP[2]=2, LCP[3]=3
        assertArrayEquals(new int[]{0, 1, 2, 3}, new SuffixArrayBuilder("aaaa").getLcpArray());
    }

    @Test
    public void testLCPSingleChar() {
        assertArrayEquals(new int[]{0}, new SuffixArrayBuilder("a").getLcpArray());
    }

    @Test
    public void testLCPEmpty() {
        assertArrayEquals(new int[]{}, new SuffixArrayBuilder("").getLcpArray());
    }

    @Test
    public void testLCPAllDistinct() {
        // "abcd": no two adjacent suffixes share a prefix -> all LCP = 0 except LCP[0]
        String text = "abcd";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        int[] lcp = sab.getLcpArray();
        assertEquals(0, lcp[0]);
        // Each suffix starts with a different character, so all LCPs are 0
        for (int v : lcp) assertEquals(0, v);
    }

    @Test
    public void testLCPMatchesBruteForce() {
        for (String text : new String[]{"mississippi", "abracadabra", "ababab", "aabbaab"}) {
            SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
            int[] sa  = sab.getSuffixArray();
            int[] lcp = sab.getLcpArray();
            int[] expected = bruteForceLCP(text, sa);
            assertArrayEquals(expected, lcp, "LCP mismatch for text=\"" + text + "\"");
        }
    }

    @Test
    public void testLCPHasCorrectLength() {
        String text = "algorithm";
        assertEquals(text.length(), new SuffixArrayBuilder(text).getLcpArray().length);
    }

    @Test
    public void testLCPFirstElementIsAlwaysZero() {
        for (String text : new String[]{"banana", "abc", "aaa", "z"}) {
            assertEquals(0, new SuffixArrayBuilder(text).getLcpArray()[0],
                    "LCP[0] must be 0 for text=\"" + text + "\"");
        }
    }

    // =========================================================
    // Section 4: Pattern Search via Binary Search on SA
    // =========================================================

    @Test
    public void testSearchBasic() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        CustomArrayList<Integer> result = sab.search("ana");
        assertEquals(2, result.size());
        assertTrue(containsIndex(result, 1));
        assertTrue(containsIndex(result, 3));
    }

    @Test
    public void testSearchNoMatch() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        CustomArrayList<Integer> result = sab.search("xyz");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchSingleChar() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        CustomArrayList<Integer> result = sab.search("a");
        assertEquals(3, result.size());
        assertTrue(containsIndex(result, 1));
        assertTrue(containsIndex(result, 3));
        assertTrue(containsIndex(result, 5));
    }

    @Test
    public void testSearchPatternEqualsText() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("hello");
        CustomArrayList<Integer> result = sab.search("hello");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testSearchEmptyPattern() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("banana");
        assertTrue(sab.search("").isEmpty());
        assertTrue(sab.search(null).isEmpty());
    }

    @Test
    public void testSearchAllSameChars() {
        SuffixArrayBuilder sab = new SuffixArrayBuilder("aaaa");
        CustomArrayList<Integer> result = sab.search("aa");
        // "aa" appears at indices 0, 1, 2
        assertEquals(3, result.size());
        assertTrue(containsIndex(result, 0));
        assertTrue(containsIndex(result, 1));
        assertTrue(containsIndex(result, 2));
    }

    @Test
    public void testSearchResultsMatchNaive() {
        String text = "abracadabra";
        SuffixArrayBuilder sab = new SuffixArrayBuilder(text);
        NaiveStringMatcher naive = new NaiveStringMatcher();

        for (String pat : new String[]{"a", "abra", "bra", "ca", "z"}) {
            CustomArrayList<Integer> saResult    = sab.search(pat);
            CustomArrayList<Integer> naiveResult = naive.search(text, pat);

            assertEquals(naiveResult.size(), saResult.size(),
                    "Count mismatch for pattern=\"" + pat + "\"");

            // SA search may return results in SA order (not text order); sort both before comparing
            int[] saArr    = toSortedArray(saResult);
            int[] naiveArr = toSortedArray(naiveResult);
            assertArrayEquals(naiveArr, saArr,
                    "Index mismatch for pattern=\"" + pat + "\"");
        }
    }

    // =========================================================
    // Utilities
    // =========================================================

    private boolean containsIndex(CustomArrayList<Integer> list, int idx) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == idx) return true;
        }
        return false;
    }

    /** Returns a sorted copy of the list as int[]. Uses insertion sort (small sizes). */
    private int[] toSortedArray(CustomArrayList<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        // insertion sort
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i], j = i - 1;
            while (j >= 0 && arr[j] > key) { arr[j + 1] = arr[j]; j--; }
            arr[j + 1] = key;
        }
        return arr;
    }
}