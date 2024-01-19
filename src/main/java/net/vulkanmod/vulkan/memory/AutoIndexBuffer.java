package net.vulkanmod.vulkan.memory;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class AutoIndexBuffer {
    int vertexCount;
    final DrawType drawType;
    IndexBuffer indexBuffer;

    public AutoIndexBuffer(int vertexCount, DrawType type) {
        this.drawType = type;

        createIndexBuffer(vertexCount);
    }

    private void createIndexBuffer(int vertexCount) {
        this.vertexCount = vertexCount;
        int size;
        ByteBuffer buffer;

        switch (drawType) {
            case QUADS -> {
                size = vertexCount * 3 / 2 * IndexBuffer.IndexType.SHORT.size;
                buffer = directAlloc(size);
            }
            case TRIANGLE_FAN -> {
                size = (vertexCount - 2) * 3 * IndexBuffer.IndexType.SHORT.size;
                buffer = directAlloc(size);
            }
            case TRIANGLE_STRIP -> {
                size = (vertexCount - 2) * 3 * IndexBuffer.IndexType.SHORT.size;
                buffer = directAlloc(size);
            }
            default -> throw new RuntimeException("unknown drawType");
        }

        indexBuffer = new IndexBuffer(buffer, MemoryTypes.GPU_MEM);
        indexBuffer.copyBuffer(buffer);

        MemoryUtil.memFree(buffer);
    }

    public void checkCapacity(int vertexCount) {
        if(vertexCount > this.vertexCount) {
            int newVertexCount = this.vertexCount * 2;
            System.out.println("Reallocating AutoIndexBuffer from " + this.vertexCount + " to " + newVertexCount);

            //TODO: free old
            //Can't know when VBO will stop using it
            indexBuffer.freeBuffer();

            // Reallocate with pre-calculated indices
            buffer = precalculatedIndices(newVertexCount);
            indexBuffer = new IndexBuffer(buffer, MemoryTypes.GPU_MEM);
            indexBuffer.copyBuffer(buffer);

            MemoryUtil.memFree(buffer);
        }
    }

    private static ByteBuffer precalculatedIndices(int vertexCount) {
        //short[] idxs = {0, 1, 2, 0, 2, 3};

        int indexCount = vertexCount * 3 / 2;
        ByteBuffer buffer = directAlloc(indexCount * Short.BYTES);
        ShortBuffer idxs = buffer.asShortBuffer();

        int j = 0;
        for(int i = 0; i < vertexCount; i += 4) {

            idxs.put(j, (short) i);
            idxs.put(j + 1, (short) (i + 1));
            idxs.put(j + 2, (short) (i + 2));
            idxs.put(j + 3, (short) (i));
            idxs.put(j + 4, (short) (i + 2));
            idxs.put(j + 5, (short) (i + 3));

            j += 6;
        }

        return buffer;
    }

    private static ByteBuffer directAlloc(int size) {
        return MemoryUtil.memAlloc(size, MemoryType.HOST_VISIBLE, MemoryType.HOST_COHERENT);
    }

    public IndexBuffer getIndexBuffer() { return indexBuffer; }

    public enum DrawType {
        QUADS(7),
        TRIANGLE_FAN(6),
        TRIANGLE_STRIP(5);

        public final int n;

        DrawType (int n) {
            this.n = n;
        }
    }
}
