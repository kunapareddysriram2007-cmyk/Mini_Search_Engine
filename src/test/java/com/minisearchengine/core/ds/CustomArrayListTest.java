package com.minisearchengine.core.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomArrayListTest {

    @Test
    public void testAddAndGet() {
        CustomArrayList<String> list = new CustomArrayList<>();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());

        list.add("Alpha");
        list.add("Beta");
        list.add("Gamma");

        assertEquals(3, list.size());
        assertFalse(list.isEmpty());
        assertEquals("Alpha", list.get(0));
        assertEquals("Beta", list.get(1));
        assertEquals("Gamma", list.get(2));
    }

    @Test
    public void testExpansionBeyondInitialCapacity() {
        CustomArrayList<Integer> list = new CustomArrayList<>(2);
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    public void testInsertAtIndex() {
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("A");
        list.add("C");
        list.add(1, "B");

        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
    }

    @Test
    public void testRemoveByIndexAndElement() {
        CustomArrayList<String> list = new CustomArrayList<>();
        list.add("One");
        list.add("Two");
        list.add("Three");

        String removed = list.remove(1);
        assertEquals("Two", removed);
        assertEquals(2, list.size());
        assertEquals("Three", list.get(1));

        assertTrue(list.removeElement("One"));
        assertFalse(list.removeElement("NotFound"));
        assertEquals(1, list.size());
        assertEquals("Three", list.get(0));
    }

    @Test
    public void testBoundsExceptions() {
        CustomArrayList<Integer> list = new CustomArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, 10));
    }
}
