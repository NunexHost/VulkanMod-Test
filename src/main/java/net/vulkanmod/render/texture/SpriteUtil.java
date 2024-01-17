package net.vulkanmod.render.texture;

import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.ArrayList;
import java.util.List;

public abstract class SpriteUtil {

    private static boolean doUpload = false;

    private static List<VulkanImage> transitionedLayouts = new ArrayList<>();

    public static void setDoUpload(boolean b) {
        doUpload = b;
    }

    public static boolean shouldUpload() {
        return doUpload;
    }

    public static void addTransitionedLayout(VulkanImage image) {
        transitionedLayouts.add(image);
    }

    public static void transitionLayouts(VkCommandBuffer commandBuffer) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            // Batch layout transitions
            for (VulkanImage image : transitionedLayouts) {
                if (image.getImageLayout() != VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                    image.readOnlyLayout(stack, commandBuffer);
                }
            }

            // Clear the list
            transitionedLayouts.clear();
        }
    }
}
