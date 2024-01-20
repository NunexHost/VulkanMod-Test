package net.vulkanmod.vulkan.memory;

import net.vulkanmod.vulkan.*;
import net.vulkanmod.vulkan.queue.CommandPool;

import java.nio.ByteBuffer;

import static net.vulkanmod.vulkan.queue.Queue.TransferQueue;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT;

public class IndirectBuffer extends Buffer {
    CommandPool.CommandBuffer commandBuffer;

    public IndirectBuffer(int size) {
        super(VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT,
                MemoryType.of(VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT),
                MemoryHeap.of(VK_MEMORY_HEAP_DEVICE_LOCAL_BIT));
        this.createBuffer(size);
    }

    public void recordCopyCmd(ByteBuffer byteBuffer) {
        int size = byteBuffer.remaining();

        if (size > this.bufferSize - this.usedBytes) {
            this.resizeBuffer(size);
        }

        if (this.type.mappable()) {
            this.type.copyToBuffer(this, size, byteBuffer);
        } else {
            if (commandBuffer == null) {
                commandBuffer = DeviceManager.getTransferQueue().beginCommands();
            }

            TransferQueue.uploadBufferCmd(commandBuffer, byteBuffer, 0, this.getId(), this.getUsedBytes(), size);
        }

        offset = usedBytes;
        usedBytes += size;
    }

    private void resizeBuffer(int size) {
        MemoryManager.getInstance().addToFreeable(this);
        int newSize = size;
        this.createBuffer(newSize);
        this.usedBytes = 0;
    }

    public void submitUploads() {
        if (commandBuffer != null) {
            DeviceManager.getTransferQueue().submitCommands(commandBuffer);
            Synchronization.INSTANCE.addCommandBuffer(commandBuffer);
            commandBuffer = null;
        }
    }

    //debug
    public ByteBuffer getByteBuffer() {
        return this.data.getByteBuffer(0, this.bufferSize);
    }

    @Override
    public void destroy() {
        super.destroy();

        if (commandBuffer != null) {
            DeviceManager.getTransferQueue().freeCommandBuffer(commandBuffer);
        }
    }

    @Override
    public void handleError(int err) {
        super.handleError(err);

        if (err == VK_ERROR_INVALID_EXTERNAL_HANDLE) {
            // handle invalid external handle error
        }
    }
}
