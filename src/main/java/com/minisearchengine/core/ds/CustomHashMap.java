package com.minisearchengine.core.ds;

/**
 * Custom Hash Map built from scratch using separate chaining with singly linked lists.
 * 
 * Strict constraint: Zero java.util.* imports.
 * 
 * Design & Mechanics:
 * - Buckets: Array of Entry<K, V> heads.
 * - Hash distribution: Uses Math.abs(key.hashCode()) % capacity, with bit-mixing.
 * - Load factor: 0.75f default. When size >= capacity * loadFactor, table doubles and rehashes.
 * 
 * Time Complexities:
 * - Put: Average O(1), Worst O(n) under pathological hash collisions.
 * - Get: Average O(1), Worst O(n).
 * - Remove: Average O(1), Worst O(n).
 * - ContainsKey: Average O(1), Worst O(n).
 * 
 * Space Complexity:
 * - O(N + M) where N is the number of entries and M is bucket capacity.
 * 
 * @param <K> Key type
 * @param <V> Value type
 */
public class CustomHashMap<K, V> {

    public static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        public Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
    private int size = 0;
    private final float loadFactor;

    public CustomHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public CustomHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.buckets = (Entry<K, V>[]) new Entry[initialCapacity];
        this.loadFactor = loadFactor;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        // Spread higher bits to lower bits
        h = h ^ (h >>> 16);
        return (h & 0x7FFFFFFF) % buckets.length;
    }

    public V put(K key, V value) {
        if ((float) (size + 1) / buckets.length > loadFactor) {
            resize(buckets.length * 2);
        }

        int index = hash(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (keysEqual(current.key, key)) {
                V old = current.value;
                current.value = value;
                return old;
            }
            current = current.next;
        }

        // Prepend new entry
        buckets[index] = new Entry<>(key, value, buckets[index]);
        size++;
        return null;
    }

    public V get(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if (keysEqual(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public V getOrDefault(K key, V defaultValue) {
        V val = get(key);
        return val != null ? val : defaultValue;
    }

    public boolean containsKey(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        while (current != null) {
            if (keysEqual(current.key, key)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> prev = null;

        while (current != null) {
            if (keysEqual(current.key, key)) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    public CustomArrayList<K> keyList() {
        CustomArrayList<K> keys = new CustomArrayList<>(size);
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> cur = buckets[i];
            while (cur != null) {
                keys.add(cur.key);
                cur = cur.next;
            }
        }
        return keys;
    }

    public CustomArrayList<Entry<K, V>> entryList() {
        CustomArrayList<Entry<K, V>> entries = new CustomArrayList<>(size);
        for (int i = 0; i < buckets.length; i++) {
            Entry<K, V> cur = buckets[i];
            while (cur != null) {
                entries.add(cur);
                cur = cur.next;
            }
        }
        return entries;
    }

    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = null;
        }
        size = 0;
    }

    @SuppressWarnings("unchecked")
    private void resize(int newCapacity) {
        Entry<K, V>[] oldBuckets = buckets;
        buckets = (Entry<K, V>[]) new Entry[newCapacity];
        size = 0;

        for (int i = 0; i < oldBuckets.length; i++) {
            Entry<K, V> cur = oldBuckets[i];
            while (cur != null) {
                put(cur.key, cur.value);
                cur = cur.next;
            }
        }
    }

    private boolean keysEqual(K k1, K k2) {
        if (k1 == null && k2 == null) return true;
        if (k1 == null || k2 == null) return false;
        return k1.equals(k2);
    }
}