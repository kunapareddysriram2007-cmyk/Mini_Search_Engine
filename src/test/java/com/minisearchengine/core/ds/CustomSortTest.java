package com.minisearchengine.core.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomSortTest {

    @Test
    public void testRandomizedQuickSort() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        int[] vals = {64, 25, 12, 22, 11, 90, 88, 1, 45, 23};
        for (int v : vals) {
            list.add(v);
        }

        CustomSort.randomizedQuickSort(list);

        assertEquals(vals.length, list.size());
        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i) <= list.get(i + 1), "List should be sorted ascending");
        }
        assertEquals(1, list.get(0));
        assertEquals(90, list.get(list.size() - 1));
    }

    @Test
    public void testSortEmptyAndSingleElement() {
        CustomArrayList<Integer> empty = new CustomArrayList<>();
        CustomSort.randomizedQuickSort(empty);
        assertEquals(0, empty.size());

        CustomArrayList<String> single = new CustomArrayList<>();
        single.add("single");
        CustomSort.randomizedQuickSort(single);
        assertEquals("single", single.get(0));
    }

    @Test
    public void testCustomComparatorDescendingOrder() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        int[] vals = {5, 1, 9, 3, 7, 2, 8, 4, 6};
        for (int v : vals) {
            list.add(v);
        }

        // Sort descending using CustomComparator
        CustomSort.randomizedQuickSort(list, (a, b) -> Integer.compare(b, a));

        assertEquals(vals.length, list.size());
        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(list.get(i) >= list.get(i + 1), "List should be sorted descending");
        }
        assertEquals(9, list.get(0));
        assertEquals(1, list.get(list.size() - 1));
    }

    @Test
    public void testNonComparableObjectsWithCustomComparator() {
        class NonComparableItem {
            final String name;
            final int score;
            NonComparableItem(String name, int score) {
                this.name = name;
                this.score = score;
            }
        }

        CustomArrayList<NonComparableItem> list = new CustomArrayList<>();
        list.add(new NonComparableItem("Charlie", 70));
        list.add(new NonComparableItem("Alice", 95));
        list.add(new NonComparableItem("Bob", 85));

        // Sort by score ascending
        CustomSort.randomizedQuickSort(list, (a, b) -> Integer.compare(a.score, b.score));

        assertEquals(3, list.size());
        assertEquals("Charlie", list.get(0).name);
        assertEquals("Bob", list.get(1).name);
        assertEquals("Alice", list.get(2).name);
    }
}