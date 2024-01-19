package net.vulkanmod.render.chunk.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class ResettableQueue<T> implements Iterable<T> {

    /**
     * The underlying array.
     */
    private T[] queue;

    /**
     * The index of the next element to be removed.
     */
    private int head;

    /**
     * The index of the last element added.
     */
    private int tail;

    /**
     * The capacity of the queue.
     */
    private int capacity;

    /**
     * Constructs a new queue with an initial capacity of 1024 elements.
     */
    public ResettableQueue() {
        this(1024);
    }

    /**
     * Constructs a new queue with the specified initial capacity.
     *
     * @param initialCapacity The initial capacity of the queue.
     */
    public ResettableQueue(int initialCapacity) {
        this.capacity = initialCapacity;
        this.queue = (T[]) new Object[capacity];
    }

    /**
     * Returns whether the queue is empty.
     *
     * @return True if the queue is empty, false otherwise.
     */
    public boolean isEmpty() {
        return head == tail;
    }

    /**
     * Returns the number of elements in the queue.
     *
     * @return The number of elements in the queue.
     */
    public int size() {
        return tail - head;
    }

    /**
     * Returns the next element in the queue without removing it.
     *
     * @return The next element in the queue.
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return queue[head];
    }

    /**
     * Removes and returns the next element in the queue.
     *
     * @return The next element in the queue.
     */
    public T poll() {
        if (isEmpty()) {
            return null;
        }
        T element = queue[head];
        queue[head] = null;
        head = (head + 1) % capacity;
        return element;
    }

    /**
     * Adds an element to the end of the queue.
     *
     * @param element The element to add.
     */
    public void add(T element) {
        if (element == null) {
            return;
        }
        if (tail == capacity) {
            grow();
        }
        queue[tail] = element;
        tail = (tail + 1) % capacity;
    }

    /**
     * Clears the queue.
     */
    public void clear() {
        head = 0;
        tail = 0;
        for (int i = 0; i < capacity; i++) {
            queue[i] = null;
        }
    }

    /**
     * Returns an iterator for the queue.
     *
     * @return An iterator for the queue.
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            int pos = head;

            @Override
            public boolean hasNext() {
                return pos != tail;
            }

            @Override
            public T next() {
                T element = queue[pos];
                pos = (pos + 1) % capacity;
                return element;
            }
        };
    }

    /**
     * Grows the capacity of the queue by a factor of 1.5.
     */
    private void grow() {
        capacity *= 1.5;
        T[] newQueue = (T[]) new Object[capacity];
        System.arraycopy(queue, head, newQueue, 0, tail - head);
        System.arraycopy(queue, 0, newQueue, tail - head, head);
        queue = newQueue;
        head = 0;
        tail = size();
    }
}
