package com.minisearchengine.core.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomHashMapAndSetTest {

    @Test
    public void testHashMapOperations() {
        CustomHashMap<String, Integer> map = new CustomHashMap<>(4, 0.75f);
        assertTrue(map.isEmpty());

        map.put("cat", 1);
        map.put("dog", 2);
        map.put("bird", 3);
        map.put("elephant", 4); // triggers resize

        assertEquals(4, map.size());
        assertEquals(1, map.get("cat"));
        assertEquals(2, map.get("dog"));
        assertEquals(3, map.get("bird"));
        assertEquals(4, map.get("elephant"));
        assertNull(map.get("lion"));

        // update existing key
        Integer old = map.put("cat", 10);
        assertEquals(1, old);
        assertEquals(10, map.get("cat"));
        assertEquals(4, map.size());

        // remove
        assertEquals(2, map.remove("dog"));
        assertNull(map.get("dog"));
        assertEquals(3, map.size());
    }

    @Test
    public void testHashSetOperations() {
        CustomHashSet<String> set = new CustomHashSet<>();
        assertTrue(set.add("apple"));
        assertTrue(set.add("banana"));
        assertFalse(set.add("apple")); // duplicate
        assertEquals(2, set.size());

        assertTrue(set.contains("apple"));
        assertTrue(set.contains("banana"));
        assertFalse(set.contains("cherry"));

        assertTrue(set.remove("apple"));
        assertFalse(set.contains("apple"));
        assertEquals(1, set.size());
    }

    @Test
    public void testKeyWithIntegerMinValueHashCode() {
        class MinHashKey {
            private final int id;
            public MinHashKey(int id) { this.id = id; }
            @Override
            public int hashCode() {
                return Integer.MIN_VALUE;
            }
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                MinHashKey other = (MinHashKey) o;
                return this.id == other.id;
            }
        }

        CustomHashMap<MinHashKey, String> map = new CustomHashMap<>();
        MinHashKey k1 = new MinHashKey(1);
        MinHashKey k2 = new MinHashKey(2);

        // Put should not throw ArrayIndexOutOfBoundsException
        assertDoesNotThrow(() -> {
            map.put(k1, "Val1");
            map.put(k2, "Val2");
        });

        assertEquals(2, map.size());
        assertEquals("Val1", map.get(k1));
        assertEquals("Val2", map.get(k2));
        assertTrue(map.containsKey(k1));
        assertEquals("Val1", map.remove(k1));
        assertNull(map.get(k1));
        assertEquals(1, map.size());
    }
}