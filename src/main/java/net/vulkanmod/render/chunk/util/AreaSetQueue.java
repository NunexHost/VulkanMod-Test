package net.vulkanmod.render.chunk.util;

import net.vulkanmod.render.chunk.ChunkArea;

import java.util.Arrays;
import java.util.Iterator;

public record AreaSetQueue(int size, int[] set, StaticQueue<ChunkArea> queue)
{

    public AreaSetQueue(int size) {
        this(size, new int[(int) Math.ceil((float)size / 64)], new StaticQueue<>(size));
    }

    /**
     * Optimizes `add` method by using bit twiddling and pre-calculating the `i` value.
     */
    public void add(ChunkArea chunkArea) {
        if (chunkArea.index() >= this.size)
            throw new IndexOutOfBoundsException();

        int i = chunkArea.index() >> 6;
        if ((this.set[i] & (1 << (chunkArea.index() & 63))) == 0) {
            this.set[i] |= (1 << (chunkArea.index() & 63));
            this.queue.add(chunkArea);
        }
    }

    /**
     * Optimizes `clear` method by directly assigning a new zero-filled array to `set`.
     */
    public void clear() {
        this.set = new int[this.size >> 6];
        this.queue.clear();
    }

    public Iterator<ChunkArea> iterator(boolean reverseOrder) {
        return this.queue.iterator(reverseOrder);
    }
}
