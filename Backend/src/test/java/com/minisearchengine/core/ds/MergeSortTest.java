package com.minisearchengine.core.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the hand-written stable MergeSort (no java.util).
 */
public class MergeSortTest {

    @Test
    public void testSortsIntegersAscending() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        int[] vals = {5, 1, 9, 3, 7, 2, 8, 4, 6};
        for (int v : vals) list.add(v);

        MergeSort.sort(list, Integer::compare);

        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i) <= list.get(i + 1));
        }
        assertEquals(1, list.get(0));
        assertEquals(9, list.get(list.size() - 1));
    }

    @Test
    public void testStabilityOnEqualKeys() {
        // Records with equal keys must keep insertion order (stability).
        class Rec {
            final int key;
            final String label;
            Rec(int key, String label) { this.key = key; this.label = label; }
        }
        CustomArrayList<Rec> list = new CustomArrayList<>();
        list.add(new Rec(1, "first"));
        list.add(new Rec(0, "a"));
        list.add(new Rec(1, "second"));
        list.add(new Rec(0, "b"));
        list.add(new Rec(1, "third"));

        MergeSort.sort(list, (x, y) -> Integer.compare(x.key, y.key));

        assertEquals("a", list.get(0).label);
        assertEquals("b", list.get(1).label);
        assertEquals("first", list.get(2).label);
        assertEquals("second", list.get(3).label);
        assertEquals("third", list.get(4).label);
    }

    @Test
    public void testAlreadySortedAndReverseSorted() {
        CustomArrayList<Integer> asc = new CustomArrayList<>();
        for (int i = 0; i < 20; i++) asc.add(i);
        MergeSort.sort(asc, Integer::compare);
        for (int i = 0; i < 20; i++) assertEquals(i, asc.get(i));

        CustomArrayList<Integer> desc = new CustomArrayList<>();
        for (int i = 20; i > 0; i--) desc.add(i);
        MergeSort.sort(desc, Integer::compare);
        for (int i = 0; i < 20; i++) assertEquals(i + 1, desc.get(i));
    }

    @Test
    public void testEmptySingleAndDuplicates() {
        CustomArrayList<Integer> empty = new CustomArrayList<>();
        MergeSort.sort(empty, Integer::compare);
        assertTrue(empty.isEmpty());

        CustomArrayList<Integer> single = new CustomArrayList<>();
        single.add(42);
        MergeSort.sort(single, Integer::compare);
        assertEquals(42, single.get(0));

        CustomArrayList<Integer> dups = new CustomArrayList<>();
        for (int i = 0; i < 5; i++) { dups.add(7); dups.add(3); }
        MergeSort.sort(dups, Integer::compare);
        for (int i = 0; i < 5; i++) assertEquals(3, dups.get(i));
        for (int i = 5; i < 10; i++) assertEquals(7, dups.get(i));
    }

    @Test
    public void testStringsLexicographic() {
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("pear"); list.add("apple"); list.add("mango"); list.add("apple");
        MergeSort.sort(list, String::compareTo);
        assertEquals("apple", list.get(0));
        assertEquals("apple", list.get(1));
        assertEquals("mango", list.get(2));
        assertEquals("pear", list.get(3));
    }

    @Test
    public void testNullArgumentsThrow() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> MergeSort.sort(null, Integer::compare));
        assertThrows(IllegalArgumentException.class, () -> MergeSort.sort(list, null));
    }
}
