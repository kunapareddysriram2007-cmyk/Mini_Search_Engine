package com.minisearchengine.core.string;

import com.minisearchengine.core.ds.CustomArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NaiveStringMatcherTest {

    private NaiveStringMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new NaiveStringMatcher();
    }

    @Test
    public void testBasicMatch() {
        CustomArrayList<Integer> result = matcher.search("hello world", "world");
        assertEquals(1, result.size());
        assertEquals(6, result.get(0));
    }

    @Test
    public void testMultipleMatches() {
        CustomArrayList<Integer> result = matcher.search("abracadabra", "abra");
        assertEquals(2, result.size());
        assertEquals(0, result.get(0));
        assertEquals(7, result.get(1));
    }

    @Test
    public void testOverlappingMatches() {
        // In "AAAAA", pattern "AAA" occurs at index 0, 1, and 2
        CustomArrayList<Integer> result = matcher.search("AAAAA", "AAA");
        assertEquals(3, result.size());
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
    }

    @Test
    public void testNoMatch() {
        CustomArrayList<Integer> result = matcher.search("quick brown fox", "lazy");
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    public void testPatternLongerThanText() {
        CustomArrayList<Integer> result = matcher.search("short", "longer_pattern");
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    public void testPatternEqualToText() {
        CustomArrayList<Integer> result = matcher.search("exact_match", "exact_match");
        assertEquals(1, result.size());
        assertEquals(0, result.get(0));
    }

    @Test
    public void testEmptyPattern() {
        CustomArrayList<Integer> result = matcher.search("some text", "");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSingleCharacterPattern() {
        CustomArrayList<Integer> result = matcher.search("banana", "a");
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(3, result.get(1));
        assertEquals(5, result.get(2));
    }

    @Test
    public void testNullHandling() {
        assertTrue(matcher.search(null, "pattern").isEmpty());
        assertTrue(matcher.search("text", null).isEmpty());
        assertTrue(matcher.search(null, null).isEmpty());
    }
}