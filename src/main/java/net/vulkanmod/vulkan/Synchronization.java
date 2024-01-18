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

    private LongBuffer fences;
    private int idx = 0;

    // Potentially unnecessary storage of commandBuffers (reconsider based on use cases)
    private final ConcurrentLinkedQueue<CommandPool.CommandBuffer> commandBuffers = new ConcurrentLinkedQueue<>();

    private Synchronization() {
        this.fences = MemoryUtil.memAllocLong(ALLOCATION_SIZE_DEFAULT);
    }

    public synchronized void addCommandBuffer(CommandPool.CommandBuffer commandBuffer) {
        this.addFence(commandBuffer.getFence());
        this.commandBuffers.add(commandBuffer);
    }

    // ... addFence method (already provided) ...

    public synchronized void waitFences() {

        if (idx == 0) return;

        // Wait for all fences (already provided)
        VkDevice device = Vulkan.getDevice();
        vkWaitForFences(device, fences.address(), idx, true, VUtil.UINT64_MAX);

        // Reset command buffers (if still needed)
        commandBuffers.forEach(CommandPool.CommandBuffer::reset);

        // Clear the buffer
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
