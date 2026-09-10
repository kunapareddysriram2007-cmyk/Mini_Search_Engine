package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KMPStringMatcherTest {

    private KMPStringMatcher kmp;
    private NaiveStringMatcher naive;

    @BeforeEach
    public void setUp() {
        kmp   = new KMPStringMatcher();
        naive = new NaiveStringMatcher();
    }

    // =========================================================
    // Section 1: LPS Array Correctness
    // =========================================================

    @Test
    public void testLPSAllDistinct() {
        // "ABCDE" -> all lps values must be 0 (no repeating prefix)
        int[] lps = kmp.buildLPS("ABCDE");
        assertArrayEquals(new int[]{0, 0, 0, 0, 0}, lps);
    }

    @Test
    public void testLPSRepeatingChars() {
        // "AAAA" -> lps = [0, 1, 2, 3]
        int[] lps = kmp.buildLPS("AAAA");
        assertArrayEquals(new int[]{0, 1, 2, 3}, lps);
    }

    @Test
    public void testLPSMixedPattern() {
        // "ABABAB" -> lps = [0, 0, 1, 2, 3, 4]
        int[] lps = kmp.buildLPS("ABABAB");
        assertArrayEquals(new int[]{0, 0, 1, 2, 3, 4}, lps);
    }

    @Test
    public void testLPSSingleChar() {
        int[] lps = kmp.buildLPS("A");
        assertArrayEquals(new int[]{0}, lps);
    }

    @Test
    public void testLPSABCABD() {
        // "ABCABD" -> lps = [0, 0, 0, 1, 2, 0]
        int[] lps = kmp.buildLPS("ABCABD");
        assertArrayEquals(new int[]{0, 0, 0, 1, 2, 0}, lps);
    }

    // =========================================================
    // Section 2: KMP Search Correctness
    // =========================================================

    @Test
    public void testBasicMatch() {
        CustomArrayList<Integer> result = kmp.search("hello world", "world");
        assertEquals(1, result.size());
        assertEquals(6, result.get(0));
    }

    @Test
    public void testMultipleMatches() {
        CustomArrayList<Integer> result = kmp.search("abracadabra", "abra");
        assertEquals(2, result.size());
        assertEquals(0, result.get(0));
        assertEquals(7, result.get(1));
    }

    @Test
    public void testOverlappingMatches() {
        // "AAAAA" with pattern "AAA" -> indices 0, 1, 2
        CustomArrayList<Integer> result = kmp.search("AAAAA", "AAA");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    public void testNoMatch() {
        CustomArrayList<Integer> result = kmp.search("quick brown fox", "lazy");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPatternLongerThanText() {
        CustomArrayList<Integer> result = kmp.search("short", "longer_pattern_here");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPatternEqualsText() {
        CustomArrayList<Integer> result = kmp.search("exact_match", "exact_match");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testEmptyPattern() {
        CustomArrayList<Integer> result = kmp.search("some text", "");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleCharPattern() {
        CustomArrayList<Integer> result = kmp.search("banana", "a");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(5, result.get(2));
    }

    @Test
    public void testRepeatedCharPattern() {
        // "AABAAB" in "AABAABAABAAB"
        CustomArrayList<Integer> result = kmp.search("AABAABAABAAB", "AABAAB");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(6, result.get(2));
    }

    @Test
    public void testNullHandling() {
        assertTrue(kmp.search(null, "pattern").isEmpty());
        assertTrue(kmp.search("text", null).isEmpty());
        assertTrue(kmp.search(null, null).isEmpty());
    }

    // =========================================================
    // Section 3: KMP vs Naive Comparison
    // Cross-validate that both algorithms produce identical results
    // =========================================================

    private void assertKMPMatchesNaive(String text, String pattern) {
        CustomArrayList<Integer> kmpResult   = kmp.search(text, pattern);
        CustomArrayList<Integer> naiveResult = naive.search(text, pattern);

        assertEquals(naiveResult.size(), kmpResult.size(),
                "Match count differs for text=\"" + text + "\" pattern=\"" + pattern + "\"");

        for (int i = 0; i < naiveResult.size(); i++) {
            assertEquals(naiveResult.get(i), kmpResult.get(i),
                    "Index mismatch at position " + i
                            + " for text=\"" + text + "\" pattern=\"" + pattern + "\"");
        }
    }

    @Test
    public void testKMPvsNaive_BasicMatch() {
        assertKMPMatchesNaive("hello world", "world");
    }

    @Test
    public void testKMPvsNaive_MultipleMatches() {
        assertKMPMatchesNaive("abracadabra", "abra");
    }

    @Test
    public void testKMPvsNaive_Overlapping() {
        assertKMPMatchesNaive("AAAAA", "AAA");
    }

    @Test
    public void testKMPvsNaive_RepeatedPattern() {
        assertKMPMatchesNaive("AABAABAABAAB", "AABAAB");
    }

    @Test
    public void testKMPvsNaive_NoMatch() {
        assertKMPMatchesNaive("quick brown fox", "lazy");
    }

    @Test
    public void testKMPvsNaive_SingleChar() {
        assertKMPMatchesNaive("banana", "a");
    }

    @Test
    public void testKMPvsNaive_PatternEqualsText() {
        assertKMPMatchesNaive("exactly_this", "exactly_this");
    }

    @Test
    public void testKMPvsNaive_WhitespacePattern() {
        assertKMPMatchesNaive("a b c a b c", "a b");
    }

    @Test
    public void testKMPvsNaive_LongRepeatingText() {
        // Worst-case style input for Naive; KMP should still match
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) sb.append("AB");
        assertKMPMatchesNaive(sb.toString(), "ABAB");
    }
}