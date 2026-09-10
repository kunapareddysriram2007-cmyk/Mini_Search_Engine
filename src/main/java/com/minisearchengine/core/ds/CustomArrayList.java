package com.minisearchengine.core.ds;

/**
 * Custom dynamic array (ArrayList equivalent) built entirely from scratch.
 * 
 * Strict constraint: No java.util.* imports.
 * 
 * Time Complexities:
 * - Append (add): Amortized O(1), Worst O(n) during resizing.
 * - Get by index: O(1).
 * - Set by index: O(1).
 * - Remove by index: O(n) due to shifting elements.
 * - Contains / IndexOf: O(n) linear search.
 * 
 * Space Complexity:
 * - O(n) where n is current capacity. Resizing factor is 2x.
 *
 * @param <T> Element type
 */
public class CustomArrayList<T> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private Object[] elements;
    private int size;

    public CustomArrayList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    public CustomArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        }
        this.elements = new Object[Math.max(1, initialCapacity)];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void add(T element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
    }

    public void add(int index, T element) {
        checkPositionIndex(index);
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            elements[i] = elements[i - 1];
        }
        elements[index] = element;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkElementIndex(index);
        return (T) elements[index];
    }

    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
        checkElementIndex(index);
        T old = (T) elements[index];
        elements[index] = element;
        return old;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkElementIndex(index);
        T oldValue = (T) elements[index];
        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }
        elements[--size] = null; // avoid memory leak
        return oldValue;
    }

    public boolean removeElement(T element) {
        int idx = indexOf(element);
        if (idx >= 0) {
            remove(idx);
            return true;
        }
        return false;
    }

    public int indexOf(T element) {
        if (element == null) {
            for (int i = 0; i < size; i++) {
                if (elements[i] == null) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (element.equals(elements[i])) return i;
            }
        }
        return -1;
    }

    public boolean contains(T element) {
        return indexOf(element) >= 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            int newCapacity = elements.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            Object[] newArray = new Object[newCapacity];
            for (int i = 0; i < size; i++) {
                newArray[i] = elements[i];
            }
            this.elements = newArray;
        }
    }

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
