package net.vulkanmod.vulkan.memory;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;

public class VertexBuffer extends Buffer {

    private long handle;

    public VertexBuffer(int size) {
        this(size, MemoryTypes.HOST_MEM);
    }

    public VertexBuffer(int size, MemoryType type) {
        super(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, type);

        this.bufferSize = size;
        this.usedBytes = 0;
        this.offset = 0;

        // Retrieve the handle after buffer creation
        this.handle = retrieveHandle();
    }

    // Assume this method exists in the Buffer class or is implemented here
    protected long retrieveHandle() {
        // Implement logic to retrieve the buffer handle
        return device.getBuffer(this).getHandle();
    }

    public void copyToVertexBuffer(long vertexSize, long vertexCount, ByteBuffer byteBuffer) {
        int bufferSize = (int) (vertexSize * vertexCount);

        if (bufferSize > this.bufferSize - this.usedBytes) {
            resizeBuffer(this.bufferSize + bufferSize);
        }

        // Access the handle after retrieval
        this.type.copyToBuffer(handle, (long)bufferSize, byteBuffer);
        this.offset += bufferSize;
    }

    private void resizeBuffer(int newSize) {
        MemoryManager.getInstance().addToFreeable(this);
        this.createBuffer(newSize);
        this.bufferSize = newSize;
    }

    public void destroy() {
        MemoryManager.getInstance().addToFreeable(this);
        try {
            // Perform necessary cleanup actions here, such as releasing resources
            device.freeBuffer(this);
        } catch (Exception e) {
            // Handle any exceptions that may occur during cleanup
            System.err.println("Error during VertexBuffer cleanup: " + e.getMessage());
        }
    }
}
