package com.minisearchengine.core.string;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LevenshteinDistance (DP + early-abandon threshold).
 */
public class LevenshteinDistanceTest {

    private final LevenshteinDistance lev = new LevenshteinDistance();

    @Test
    public void testClassicDistances() {
        assertEquals(3, lev.distance("kitten", "sitting"));
        assertEquals(2, lev.distance("flaw", "lawn"));
        assertEquals(1, lev.distance("cat", "cart"));
        assertEquals(1, lev.distance("cart", "cat"));
        assertEquals(3, lev.distance("abc", "xyz"));
        assertEquals(1, lev.distance("system", "systems"));
    }

    @Test
    public void testIdenticalAndEmpty() {
        assertEquals(0, lev.distance("same", "same"));
        assertEquals(4, lev.distance("", "abcd"));
        assertEquals(4, lev.distance("abcd", ""));
        assertEquals(0, lev.distance("", ""));
    }

    @Test
    public void testSymmetry() {
        String[][] pairs = {
            {"kitten", "sitting"}, {"abcd", "ab"}, {"x", "yyyyy"}, {"", "zz"},
            {"operating", "operate"}, {"index", "indices"}
        };
        for (String[] p : pairs) {
            assertEquals(lev.distance(p[0], p[1]), lev.distance(p[1], p[0]),
                    "distance must be symmetric for " + p[0] + "/" + p[1]);
        }
    }

    @Test
    public void testUnicode() {
        assertEquals(1, lev.distance("café", "cafe"));
        assertEquals(1, lev.distance("naïve", "naive"));
    }

    @Test
    public void testThresholdBasic() {
        assertTrue(lev.withinThreshold("cat", "cats", 1));
        assertTrue(lev.withinThreshold("cat", "cart", 1));
        assertFalse(lev.withinThreshold("cat", "cart", 0));
        assertFalse(lev.withinThreshold("kitten", "sitting", 2));
        assertTrue(lev.withinThreshold("kitten", "sitting", 3));
        assertTrue(lev.withinThreshold("abc", "abc", 0));
    }

    @Test
    public void testEarlyAbandonAgreesWithFullDistance() {
        // The early-abandon path must NEVER disagree with the true distance.
        String[] words = {"algorithm", "index", "search", "query", "memory",
                          "process", "kernel", "scheduler", "network", "socket"};
        String[] noisy = {"algoritm", "indeks", "serch", "qury", "memori",
                          "proces", "kernell", "schedular", "nework", "soket"};
        for (int i = 0; i < words.length; i++) {
            int d = lev.distance(words[i], noisy[i]);
            assertTrue(lev.withinThreshold(words[i], noisy[i], d),
                    "withinThreshold must accept k = d");
            assertFalse(lev.withinThreshold(words[i], noisy[i], d - 1),
                    "withinThreshold must reject k = d-1");
        }
    }

    @Test
    public void testLengthDifferenceShortCircuit() {
        // |len| difference > k must be rejected without DP.
        assertFalse(lev.withinThreshold("a", "abcdefgh", 3));
        assertTrue(lev.withinThreshold("a", "abc", 2));
    }

    @Test
    public void testNullArgumentsThrow() {
        assertThrows(IllegalArgumentException.class, () -> lev.distance(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> lev.distance("a", null));
        assertThrows(IllegalArgumentException.class, () -> lev.withinThreshold("a", "b", -1));
    }
}
