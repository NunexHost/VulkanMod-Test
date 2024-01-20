package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class QueueFamilyIndices {


    public static boolean findQueueFamilies(VkPhysicalDevice device) {

        try (MemoryStack stack = stackPush()) {

            IntBuffer queueFamilyCount = stack.ints(1);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            if (queueFamilyCount.get(0) == 1) {
                graphicsFamily = presentFamily = 0;
                return true;
            }

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.mallocStack(queueFamilyCount.get(0), stack);

            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            for (int i = 0; i < queueFamilies.capacity(); i++) {
                int queueFlags = queueFamilies.get(i).queueFlags();

                if ((queueFlags & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    graphicsFamily = i;
                } else if ((queueFlags & VK_QUEUE_COMPUTE_BIT) != 0) {
                    presentFamily = i;
                } else if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0) {
                    transferFamily = i;
                }

                if (graphicsFamily != VK_QUEUE_FAMILY_IGNORED && presentFamily != VK_QUEUE_FAMILY_IGNORED && transferFamily != VK_QUEUE_FAMILY_IGNORED) {
                    break;
                }
            }

            if (transferFamily == VK_QUEUE_FAMILY_IGNORED) {
                for (int i = 0; i < queueFamilies.capacity(); i++) {
                    int queueFlags = queueFamilies.get(i).queueFlags();

                    if ((queueFlags & VK_QUEUE_TRANSFER_BIT) != 0) {
                        transferFamily = i;
                        break;
                    }
                }
            }

            hasDedicatedTransferQueue = graphicsFamily != transferFamily;

            return graphicsFamily != VK_QUEUE_FAMILY_IGNORED && presentFamily != VK_QUEUE_FAMILY_IGNORED && transferFamily != VK_QUEUE_FAMILY_IGNORED;
        }
    }

    public enum Family {
        Graphics,
        Transfer,
        Compute
    }


    public static int graphicsFamily, presentFamily, transferFamily = VK_QUEUE_FAMILY_IGNORED;

    public static boolean hasDedicatedTransferQueue = false;

    public static boolean isComplete() {
        return graphicsFamily != VK_QUEUE_FAMILY_IGNORED && presentFamily != VK_QUEUE_FAMILY_IGNORED && transferFamily != VK_QUEUE_FAMILY_IGNORED;
    }

    public static int[] unique() {
        return IntStream.of(graphicsFamily, presentFamily, transferFamily).distinct().toArray();
    }

    public static int[] array() {
        return new int[]{graphicsFamily, presentFamily};
    }

}
