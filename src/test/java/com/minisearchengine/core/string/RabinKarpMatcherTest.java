package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RabinKarpMatcherTest {

    private RabinKarpMatcher rk;
    private NaiveStringMatcher naive;

    @BeforeEach
    public void setUp() {
        rk    = new RabinKarpMatcher();
        naive = new NaiveStringMatcher();
    }

    // =========================================================
    // Section 1: Core Search Correctness
    // =========================================================

    @Test
    public void testBasicMatch() {
        CustomArrayList<Integer> result = rk.search("hello world", "world");
        assertEquals(1, result.size());
        assertEquals(6, result.get(0));
    }

    @Test
    public void testMultipleMatches() {
        CustomArrayList<Integer> result = rk.search("abracadabra", "abra");
        assertEquals(2, result.size());
        assertEquals(0, result.get(0));
        assertEquals(7, result.get(1));
    }

    @Test
    public void testOverlappingMatches() {
        // "AAAAA" with "AAA" -> indices 0, 1, 2
        CustomArrayList<Integer> result = rk.search("AAAAA", "AAA");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    public void testNoMatch() {
        CustomArrayList<Integer> result = rk.search("quick brown fox", "lazy");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPatternLongerThanText() {
        assertTrue(rk.search("short", "longer_pattern_here").isEmpty());
    }

    @Test
    public void testPatternEqualsText() {
        CustomArrayList<Integer> result = rk.search("exact_match", "exact_match");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testEmptyPattern() {
        assertTrue(rk.search("some text", "").isEmpty());
    }

    @Test
    public void testSingleCharPattern() {
        CustomArrayList<Integer> result = rk.search("banana", "a");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(5, result.get(2));
    }

    @Test
    public void testNullHandling() {
        assertTrue(rk.search(null, "pattern").isEmpty());
        assertTrue(rk.search("text", null).isEmpty());
        assertTrue(rk.search(null, null).isEmpty());
    }

    @Test
    public void testRepeatedPattern() {
        // "AABAAB" in "AABAABAABAAB"
        CustomArrayList<Integer> result = rk.search("AABAABAABAAB", "AABAAB");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(6, result.get(2));
    }

    @Test
    public void testSingleCharText() {
        CustomArrayList<Integer> result = rk.search("A", "A");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testSpecialCharacters() {
        CustomArrayList<Integer> result = rk.search("a!b!c!b!", "!b!");
        assertEquals(2, result.size());
        assertEquals(1, result.get(0));
        assertEquals(5, result.get(1));
    }

    // =========================================================
    // Section 2: Rolling Hash Correctness
    // Verify that hash collisions (false positives) do not cause wrong output.
    // We test patterns that differ only in one character (adjacent hash values).
    // =========================================================

    @Test
    public void testFalsePositiveGuard() {
        // Pattern "ab" and "ba" have different hashes but we ensure verification
        // This purely checks correctness of verification step
        CustomArrayList<Integer> abResult = rk.search("ababab", "ab");
        assertEquals(3, abResult.size());
        assertEquals(0, abResult.get(0));
        assertEquals(2, abResult.get(1));
        assertEquals(4, abResult.get(2));

        CustomArrayList<Integer> baResult = rk.search("ababab", "ba");
        assertEquals(2, baResult.size());
        assertEquals(1, baResult.get(0));
        assertEquals(3, baResult.get(1));
    }

    // =========================================================
    // Section 3: Cross-validation against Naive
    // =========================================================

    private void assertMatchesNaive(String text, String pattern) {
        CustomArrayList<Integer> rkResult    = rk.search(text, pattern);
        CustomArrayList<Integer> naiveResult = naive.search(text, pattern);

        assertEquals(naiveResult.size(), rkResult.size(),
                "Size mismatch: text=\"" + text + "\" pattern=\"" + pattern + "\"");
        for (int i = 0; i < naiveResult.size(); i++) {
            assertEquals(naiveResult.get(i), rkResult.get(i),
                    "Index mismatch at " + i);
        }
    }

    @Test
    public void testCrossBasic()       { assertMatchesNaive("hello world",     "world"); }

    @Test
    public void testCrossMultiple()    { assertMatchesNaive("abracadabra",     "abra");  }

    @Test
    public void testCrossOverlap()     { assertMatchesNaive("AAAAA",           "AAA");   }

    @Test
    public void testCrossNoMatch()     { assertMatchesNaive("quick brown fox", "lazy");  }

    @Test
    public void testCrossSingleChar()  { assertMatchesNaive("banana",          "a");     }

    @Test
    public void testCrossRepeated()    { assertMatchesNaive("AABAABAABAAB",    "AABAAB"); }

    @Test
    public void testCrossLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("AB");
        assertMatchesNaive(sb.toString(), "ABAB");
    }
}