package com.minisearchengine.core.ds;

/**
 * Custom FIFO Queue built from scratch using a doubly linked list.
 * 
 * Strict constraint: Zero java.util.* imports.
 * 
 * Time Complexities:
 * - Enqueue (offer): O(1)
 * - Dequeue (poll): O(1)
 * - Peek: O(1)
 * 
 * Space Complexity:
 * - O(n)
 * 
 * @param <T> Element type
 */
public class CustomQueue<T> {

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(E item, Node<E> prev, Node<E> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node<T> first;
    private Node<T> last;
    private int size = 0;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void offer(T item) {
        Node<T> l = last;
        Node<T> newNode = new Node<>(item, l, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }
        size++;
    }

    public T poll() {
        if (first == null) return null;
        T item = first.item;
        Node<T> next = first.next;
        first.item = null;
        first = next;
        if (next == null) {
            last = null;
        } else {
            next.prev = null;
        }
        size--;
        return item;
    }

    public T peek() {
        return first == null ? null : first.item;
    }

    public void clear() {
        Node<T> x = first;
        while (x != null) {
            Node<T> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
    }
}