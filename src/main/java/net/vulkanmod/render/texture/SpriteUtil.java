package net.vulkanmod.render.texture;

import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.Arrays;
import java.util.Set;

public abstract class SpriteUtil {

    private static boolean doUpload = false;

    private static final Set<VulkanImage> transitionedLayouts = new HashSet<>();

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
        // Reutilize uma instância única de MemoryStack
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Agrupe transições em um único comando
            int[] imageIds = transitionedLayouts.stream().mapToInt(image -> image.getId()).toArray();
            commandBuffer.pipelineBarrier(VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    0, null, null, Arrays.stream(imageIds).mapToObj(imageId ->
                            new VkImageMemoryBarrier(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER,
                                    0, 0, imageId, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL))
                            .toArray(VkImageMemoryBarrier[]::new));

            // Limpe a lista de transições
            transitionedLayouts.clear();
        }
    }
}
