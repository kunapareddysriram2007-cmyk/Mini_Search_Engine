package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZAlgorithmMatcherTest {

    private ZAlgorithmMatcher z;
    private NaiveStringMatcher naive;
    private KMPStringMatcher kmp;

    @BeforeEach
    public void setUp() {
        z     = new ZAlgorithmMatcher();
        naive = new NaiveStringMatcher();
        kmp   = new KMPStringMatcher();
    }

    // =========================================================
    // Section 1: Z-Array Correctness
    // =========================================================

    @Test
    public void testZArrayAllDistinct() {
        // "ABCDE$ABCDE" -> Z[6..10] = 5,4,3,2,1 but we test the raw char[] build
        char[] s = "AABXAA".toCharArray();
        int[] za = z.buildZ(s);
        // Z[0]=0 by convention, Z[1]=1, Z[2]=0, Z[3]=0, Z[4]=2, Z[5]=1
        assertArrayEquals(new int[]{0, 1, 0, 0, 2, 1}, za);
    }

    @Test
    public void testZArrayAllSame() {
        char[] s = "AAAA".toCharArray();
        int[] za = z.buildZ(s);
        // Z[0]=0, Z[1]=3, Z[2]=2, Z[3]=1
        assertArrayEquals(new int[]{0, 3, 2, 1}, za);
    }

    @Test
    public void testZArrayABAB() {
        char[] s = "ABAB".toCharArray();
        int[] za = z.buildZ(s);
        // Z[0]=0, Z[1]=0, Z[2]=2, Z[3]=0
        assertArrayEquals(new int[]{0, 0, 2, 0}, za);
    }

    @Test
    public void testZArraySingleChar() {
        char[] s = "A".toCharArray();
        int[] za = z.buildZ(s);
        assertArrayEquals(new int[]{0}, za);
    }

    // =========================================================
    // Section 2: Z Search Correctness
    // =========================================================

    @Test
    public void testBasicMatch() {
        CustomArrayList<Integer> result = z.search("hello world", "world");
        assertEquals(1, result.size());
        assertEquals(6, result.get(0));
    }

    @Test
    public void testMultipleMatches() {
        CustomArrayList<Integer> result = z.search("abracadabra", "abra");
        assertEquals(2, result.size());
        assertEquals(0, result.get(0));
        assertEquals(7, result.get(1));
    }

    @Test
    public void testOverlappingMatches() {
        CustomArrayList<Integer> result = z.search("AAAAA", "AAA");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    public void testNoMatch() {
        CustomArrayList<Integer> result = z.search("quick brown fox", "lazy");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPatternLongerThanText() {
        CustomArrayList<Integer> result = z.search("short", "longer_pattern_here");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testPatternEqualsText() {
        CustomArrayList<Integer> result = z.search("exact_match", "exact_match");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testEmptyPattern() {
        assertTrue(z.search("some text", "").isEmpty());
    }

    @Test
    public void testSingleCharPattern() {
        CustomArrayList<Integer> result = z.search("banana", "a");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(5, result.get(2));
    }

    @Test
    public void testNullHandling() {
        assertTrue(z.search(null, "pattern").isEmpty());
        assertTrue(z.search("text", null).isEmpty());
        assertTrue(z.search(null, null).isEmpty());
    }

    // =========================================================
    // Section 3: Z vs Naive and Z vs KMP cross-validation
    // =========================================================

    private void assertAllAgree(String text, String pattern) {
        CustomArrayList<Integer> zRes     = z.search(text, pattern);
        CustomArrayList<Integer> naiveRes = naive.search(text, pattern);
        CustomArrayList<Integer> kmpRes   = kmp.search(text, pattern);

        assertEquals(naiveRes.size(), zRes.size(),
                "Z vs Naive size mismatch for pattern=\"" + pattern + "\"");
        assertEquals(kmpRes.size(), zRes.size(),
                "Z vs KMP size mismatch for pattern=\"" + pattern + "\"");

        for (int i = 0; i < naiveRes.size(); i++) {
            assertEquals(naiveRes.get(i), zRes.get(i),
                    "Z vs Naive index mismatch at " + i);
            assertEquals(kmpRes.get(i), zRes.get(i),
                    "Z vs KMP index mismatch at " + i);
        }
    }

    @Test
    public void testCrossValidationBasic()       { assertAllAgree("hello world",     "world"); }

    @Test
    public void testCrossValidationMultiple()    { assertAllAgree("abracadabra",     "abra");  }

    @Test
    public void testCrossValidationOverlapping() { assertAllAgree("AAAAA",           "AAA");   }

    @Test
    public void testCrossValidationNoMatch()     { assertAllAgree("quick brown fox", "lazy");  }

    @Test
    public void testCrossValidationSingleChar()  { assertAllAgree("banana",          "a");     }

    @Test
    public void testCrossValidationRepeatedPattern() {
        assertAllAgree("AABAABAABAAB", "AABAAB");
    }

    @Test
    public void testCrossValidationLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50; i++) sb.append("AB");
        assertAllAgree(sb.toString(), "ABAB");
    }
}