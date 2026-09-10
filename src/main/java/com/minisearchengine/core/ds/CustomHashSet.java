package com.minisearchengine.core.ds;

/**
 * Custom Hash Set built from scratch using CustomHashMap backing.
 * 
 * Strict constraint: Zero java.util.* imports.
 * 
 * Time Complexities:
 * - Add: Average O(1), Worst O(n).
 * - Contains: Average O(1), Worst O(n).
 * - Remove: Average O(1), Worst O(n).
 * 
 * Space Complexity:
 * - O(n) where n is number of elements.
 * 
 * @param <T> Element type
 */
public class CustomHashSet<T> {
    private static final Object DUMMY = new Object();
    private final CustomHashMap<T, Object> map;

    public CustomHashSet() {
        this.map = new CustomHashMap<>();
    }

    public CustomHashSet(int initialCapacity) {
        this.map = new CustomHashMap<>(initialCapacity, 0.75f);
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean add(T element) {
        return map.put(element, DUMMY) == null;
    }

    public boolean contains(T element) {
        return map.containsKey(element);
    }

    public boolean remove(T element) {
        return map.remove(element) != null;
    }

    public void clear() {
        map.clear();
    }

    public CustomArrayList<T> toList() {
        return map.keyList();
    }
}