package net.vulkanmod.vulkan;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.vulkan.VK10.*;

public class Synchronization {

    private static final int ALLOCATION_SIZE_DEFAULT = 50;

    public static final Synchronization INSTANCE = new Synchronization();

    private final LongBuffer fences;
    private int idx = 0;

    private final ConcurrentLinkedQueue<CommandPool.CommandBuffer> commandBuffers = new ConcurrentLinkedQueue<>();

    private Synchronization() {
        this.fences = MemoryUtil.memAllocLong(ALLOCATION_SIZE_DEFAULT);
    }

    public synchronized void addCommandBuffer(CommandPool.CommandBuffer commandBuffer) {
        this.addFence(commandBuffer.getFence());
        this.commandBuffers.add(commandBuffer);
    }

    public synchronized void addFence(long fence) {
        fences.put(idx, fence);
        idx++;

        // Check if the buffer is full and needs to be resized
        if (idx == fences.capacity()) {
            // Resize the buffer
            LongBuffer newFences = MemoryUtil.memReallocLong(fences, fences.capacity() * 2);
            fences = newFences;
        }
    }

    public synchronized void waitFences() {

        if (idx == 0) return;

        // Wait for all fences to be signaled
        VkDevice device = Vulkan.getDevice();
        vkWaitForFences(device, fences.array(), fences.limit(), true, VUtil.UINT64_MAX);

        // Reset all command buffers
        commandBuffers.forEach(CommandPool.CommandBuffer::reset);

        // Clear the buffers
        fences.clear();
        idx = 0;
    }

    public static void waitFence(long fence) {
        VkDevice device = Vulkan.getDevice();
        vkWaitForFences(device, fence, true, VUtil.UINT64_MAX);
    }

    public static boolean checkFenceStatus(long fence) {
        VkDevice device = Vulkan.getDevice();
        return vkGetFenceStatus(device, fence) == VK_SUCCESS;
    }
}
