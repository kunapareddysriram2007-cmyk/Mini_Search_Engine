package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AhoCorasickTest {

    // Helper: build AhoCorasick from a varargs of patterns
    private AhoCorasick build(String... pats) {
        CustomArrayList<String> list = new CustomArrayList<>();
        for (String p : pats) list.add(p);
        return new AhoCorasick(list);
    }

    // Helper: count matches for a specific pattern in the result list
    private int countMatches(CustomArrayList<MultiPatternMatcher.Match> matches, String pattern) {
        int count = 0;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getPattern().equals(pattern)) count++;
        }
        return count;
    }

    // Helper: get start index of the k-th match for a specific pattern
    private int startOfMatch(CustomArrayList<MultiPatternMatcher.Match> matches, String pattern, int k) {
        int seen = 0;
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getPattern().equals(pattern)) {
                if (seen == k) return matches.get(i).getStartIndex();
                seen++;
            }
        }
        return -1;
    }

    // =========================================================
    // Section 1: Single-pattern Aho-Corasick (behaves like StringMatcher)
    // =========================================================

    @Test
    public void testSinglePatternBasicMatch() {
        AhoCorasick ac = build("world");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("hello world");
        assertEquals(1, countMatches(result, "world"));
        assertEquals(6, startOfMatch(result, "world", 0));
    }

    @Test
    public void testSinglePatternNoMatch() {
        AhoCorasick ac = build("xyz");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("hello world");
        assertEquals(0, result.size());
    }

    @Test
    public void testSinglePatternMultipleOccurrences() {
        AhoCorasick ac = build("abra");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("abracadabra");
        assertEquals(2, countMatches(result, "abra"));
        assertEquals(0, startOfMatch(result, "abra", 0));
        assertEquals(7, startOfMatch(result, "abra", 1));
    }

    @Test
    public void testSinglePatternOverlapping() {
        // "AAA" in "AAAAA" -> indices 0, 1, 2
        AhoCorasick ac = build("AAA");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("AAAAA");
        assertEquals(3, countMatches(result, "AAA"));
        assertEquals(0, startOfMatch(result, "AAA", 0));
        assertEquals(1, startOfMatch(result, "AAA", 1));
        assertEquals(2, startOfMatch(result, "AAA", 2));
    }

    // =========================================================
    // Section 2: Multi-pattern matching
    // =========================================================

    @Test
    public void testMultiplePatternsAllPresent() {
        AhoCorasick ac = build("he", "she", "his", "hers");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("ushers");
        // "he"   at index 2, "she" at index 1, "hers" at index 2
        // (classic Aho-Corasick example)
        assertTrue(countMatches(result, "he")   >= 1);
        assertTrue(countMatches(result, "she")  >= 1);
        assertTrue(countMatches(result, "hers") >= 1);
    }

    @Test
    public void testMultiplePatternsPartialMatch() {
        AhoCorasick ac = build("cat", "bat", "rat");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("the cat sat on the mat");
        assertEquals(1, countMatches(result, "cat"));
        assertEquals(0, countMatches(result, "bat"));
        assertEquals(0, countMatches(result, "rat"));
    }

    @Test
    public void testMultiplePatternsOverlappingPatterns() {
        // "a" and "ab" and "abc" in "abcabc"
        AhoCorasick ac = build("a", "ab", "abc");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("abcabc");

        // "a" appears at 0 and 3
        assertEquals(2, countMatches(result, "a"));
        assertEquals(0, startOfMatch(result, "a", 0));
        assertEquals(3, startOfMatch(result, "a", 1));

        // "ab" appears at 0 and 3
        assertEquals(2, countMatches(result, "ab"));
        assertEquals(0, startOfMatch(result, "ab", 0));
        assertEquals(3, startOfMatch(result, "ab", 1));

        // "abc" appears at 0 and 3
        assertEquals(2, countMatches(result, "abc"));
        assertEquals(0, startOfMatch(result, "abc", 0));
        assertEquals(3, startOfMatch(result, "abc", 1));
    }

    @Test
    public void testPatternIsSubstringOfAnother() {
        // "he" is a suffix of "she"
        AhoCorasick ac = build("he", "she");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("she");
        assertEquals(1, countMatches(result, "she"));
        assertEquals(1, countMatches(result, "he"));  // output link propagates "he"
        assertEquals(0, startOfMatch(result, "she", 0));
        assertEquals(1, startOfMatch(result, "he",  0));
    }

    // =========================================================
    // Section 3: Edge Cases
    // =========================================================

    @Test
    public void testNullText() {
        AhoCorasick ac = build("abc");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEmptyPatternList() {
        CustomArrayList<String> empty = new CustomArrayList<>();
        AhoCorasick ac = new AhoCorasick(empty);
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("hello");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleCharPatterns() {
        AhoCorasick ac = build("a", "b");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("banana");
        assertEquals(3, countMatches(result, "a"));
        assertEquals(1, countMatches(result, "b")); // 'b' appears only at index 0 in "banana"
    }

    @Test
    public void testSingleCharPatternBananaCounts() {
        // "banana": b=1, a=3, n=2
        AhoCorasick ac = build("b", "a", "n");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("banana");
        assertEquals(1, countMatches(result, "b"));
        assertEquals(3, countMatches(result, "a"));
        assertEquals(2, countMatches(result, "n"));
    }

    @Test
    public void testPatternEqualsText() {
        AhoCorasick ac = build("hello");
        CustomArrayList<MultiPatternMatcher.Match> result = ac.searchAll("hello");
        assertEquals(1, countMatches(result, "hello"));
        assertEquals(0, startOfMatch(result, "hello", 0));
    }

    // =========================================================
    // Section 4: Cross-validate single patterns against NaiveStringMatcher
    // =========================================================

    private void assertMatchesNaive(String text, String pattern) {
        NaiveStringMatcher naive = new NaiveStringMatcher();
        CustomArrayList<Integer> naiveResult = naive.search(text, pattern);

        AhoCorasick ac = build(pattern);
        CustomArrayList<MultiPatternMatcher.Match> acResult = ac.searchAll(text);

        assertEquals(naiveResult.size(), countMatches(acResult, pattern),
                "Count mismatch for pattern=\"" + pattern + "\" text=\"" + text + "\"");
        for (int i = 0; i < naiveResult.size(); i++) {
            assertEquals(naiveResult.get(i), startOfMatch(acResult, pattern, i),
                    "Index mismatch at occurrence " + i);
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
}