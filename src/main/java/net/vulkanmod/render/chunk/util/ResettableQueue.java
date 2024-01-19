package net.vulkanmod.render.chunk.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class ResettableQueue<T> implements Iterable<T> {
    T[] queue;
    int position = 0;
    int limit = 0;
    int capacity;

    public ResettableQueue() {
        this(1024);
    }

    @SuppressWarnings("unchecked")
    public ResettableQueue(int initialCapacity) {
        this.capacity = initialCapacity;

        this.queue = (T[])(new Object[capacity]);
    }

    public boolean hasNext() {
        return this.position < this.limit;
    }

    public T poll() {
        T t = this.queue[position];
        this.position++;

        return t;
    }

    public void add(T t) {
        if (t == null) {
            return;
        }

        if (limit == capacity) {
            capacity *= 1.75;

            T[] oldQueue = this.queue;
            this.queue = (T[])(new Object[capacity]);

            System.arraycopy(oldQueue, 0, this.queue, 0, oldQueue.length);
        }

        this.queue[limit] = t;

        this.limit++;
    }

    public int size() {
        return limit;
    }

    public int capacity() {
        return capacity;
    }

    public void trim(int size) {
        if (size >= this.capacity) {
            return;
        }

        capacity = size;

        T[] oldQueue = this.queue;
        this.queue = (T[])(new Object[capacity]);

        System.arraycopy(oldQueue, 0, this.queue, 0, size);
    }

    public void clear() {
        this.position = 0;
        this.limit = 0;
    }

    public Iterator<T> iterator(boolean reverseOrder) {
        return reverseOrder ? new ReverseIterator<>(this) : new ForwardIterator<>(this);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return iterator(false);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < this.limit; ++i) {
            action.accept(this.queue[i]);
        }
    }

    private static class ForwardIterator<T> implements Iterator<T> {
        private final ResettableQueue<T> queue;
        private int position = 0;

        public ForwardIterator(ResettableQueue<T> queue) {
            this.queue = queue;
        }

        @Override
        public boolean hasNext() {
            return position < queue.limit;
        }

        @Override
        public T next() {
            T t = queue.queue[position];
            position++;

            return t;
        }
    }

    private static class ReverseIterator<T> implements Iterator<T> {
        private final ResettableQueue<T> queue;
        private int position = queue.limit - 1;

        public ReverseIterator(ResettableQueue<T> queue) {
            this.queue = queue;
            this.queue = queue.queue; // Initialize 'queue' here
            this.position = queue.limit - 1;
        }

        @Override
        public boolean hasNext() {
            return position >= 0;
        }

        @Override
        public T next() {
            T t = queue.queue[position];
            position--;

            return t;
        }
    }
}
