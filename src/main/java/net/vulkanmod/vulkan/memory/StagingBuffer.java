package net.vulkanmod.vulkan.memory;

import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.system.libc.LibCString.nmemcpy;
import static org.lwjgl.vulkan.VK10.*;

public class StagingBuffer extends Buffer {

    public StagingBuffer(int bufferSize) {
        super(VK_BUFFER_USAGE_TRANSFER_SRC_BIT, MemoryTypes.HOST_MEM);
        this.usedBytes = 0;
        this.offset = 0;

        this.createBuffer(bufferSize);
    }

    public void copyBuffer(int size, ByteBuffer byteBuffer) {

        if (size > this.bufferSize - this.usedBytes) {
            // Ensure integer calculation for resizing
            resizeBuffer((int) (this.bufferSize * 1.5));
        }

        // Use the correct memCopy method
        MemoryUtil.memCopy(MemoryUtil.memAddress(this.data.getByteBuffer(0, this.bufferSize)),
                           MemoryUtil.memAddress(byteBuffer), size);

        offset = usedBytes;
        usedBytes += size;
    }

    public void align(int alignment) {
        // Align before resizing to avoid unnecessary buffer growth
        alignBuffer(alignment);
    }

    private void resizeBuffer(int newSize) {
        MemoryManager.getInstance().addToFreeable(this);
        this.createBuffer(newSize);
    }

    private void alignBuffer(int alignment) {
        // Use `Util.align` for optimal alignment
        usedBytes = Util.align(usedBytes, alignment);
    }
}
