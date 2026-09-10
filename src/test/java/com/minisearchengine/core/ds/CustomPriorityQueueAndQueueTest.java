package com.minisearchengine.core.ds;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomPriorityQueueAndQueueTest {

    @Test
    public void testMinHeapNaturalOrdering() {
        CustomPriorityQueue<Integer> pq = new CustomPriorityQueue<>();
        pq.offer(40);
        pq.offer(10);
        pq.offer(30);
        pq.offer(20);

        assertEquals(4, pq.size());
        assertEquals(10, pq.peek());
        assertEquals(10, pq.poll());
        assertEquals(20, pq.poll());
        assertEquals(30, pq.poll());
        assertEquals(40, pq.poll());
        assertNull(pq.poll());
    }

    @Test
    public void testMaxHeapCustomComparator() {
        // Max-heap comparator
        CustomPriorityQueue<Integer> maxHeap = new CustomPriorityQueue<>((a, b) -> Integer.compare(b, a));
        maxHeap.offer(15);
        maxHeap.offer(5);
        maxHeap.offer(50);
        maxHeap.offer(25);

        assertEquals(50, maxHeap.poll());
        assertEquals(25, maxHeap.poll());
        assertEquals(15, maxHeap.poll());
        assertEquals(5, maxHeap.poll());
    }

    @Test
    public void testFIFOQueue() {
        CustomQueue<String> q = new CustomQueue<>();
        assertTrue(q.isEmpty());
        q.offer("first");
        q.offer("second");
        q.offer("third");

        assertEquals(3, q.size());
        assertEquals("first", q.peek());
        assertEquals("first", q.poll());
        assertEquals("second", q.poll());
        assertEquals("third", q.poll());
        assertNull(q.poll());
        assertTrue(q.isEmpty());
    }
}