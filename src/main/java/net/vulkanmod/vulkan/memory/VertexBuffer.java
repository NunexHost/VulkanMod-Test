package net.vulkanmod.vulkan.memory;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

public class VertexBuffer extends Buffer {

    public VertexBuffer(int size) {
        this(size, MemoryTypes.HOST_MEM);
    }

    public VertexBuffer(int size, MemoryType type) {
        super(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, type);

        this.bufferSize = size;
        this.usedBytes = 0;
        this.offset = 0;
    }

    public void copyToVertexBuffer(long vertexSize, long vertexCount, ByteBuffer byteBuffer) {
        int bufferSize = (int) (vertexSize * vertexCount);

        if (bufferSize > this.bufferSize - this.usedBytes) {
            resizeBuffer(this.bufferSize + bufferSize);
        }

        // Correct method call with appropriate arguments
        this.type.copyToBuffer(this, (long)bufferSize, byteBuffer, this.offset);
        this.offset += bufferSize;
    }

    private void resizeBuffer(int newSize) {
        MemoryManager.getInstance().addToFreeable(this);
        this.createBuffer(newSize);
        this.bufferSize = newSize;
    }

    public void destroy() {
        // Remove @Override annotation
        MemoryManager.getInstance().addToFreeable(this);
        super.destroy();
    }
}
