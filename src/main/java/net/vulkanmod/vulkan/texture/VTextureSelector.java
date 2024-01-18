package net.vulkanmod.vulkan.texture;

import net.vulkanmod.Initializer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class VTextureSelector {

    private static final int SIZE = 8;

    private static final Map<String, VulkanImage> textureCache = new HashMap<>();

    private static final VulkanImage[] boundTextures = new VulkanImage[SIZE];

    private static final int[] levels = new int[SIZE];

    private static final VulkanImage whiteTexture = VulkanImage.createWhiteTexture();

    private static int activeTexture = 0;

    public static void bindTexture(VulkanImage texture) {
        if (texture == null) {
            throw new NullPointerException("Texture cannot be null");
        }

        textureCache.put("Sampler0", texture);
        bindTexture("Sampler0");
    }

    public static void bindTexture(int i, VulkanImage texture) {
        if (i < 0 || i > 7) {
            Initializer.LOGGER.error(String.format("On Texture binding: index %d out of range [0, 7]", i));
            return;
        }

        if (texture == null) {
            throw new NullPointerException("Texture cannot be null");
        }

        textureCache.put(String.format("Sampler%d", i), texture);
        bindTexture(i);
    }

    public static void bindImage(int i, VulkanImage texture, int level) {
        if (i < 0 || i > 7) {
            Initializer.LOGGER.error(String.format("On Texture binding: index %d out of range [0, 7]", i));
            return;
        }

        if (texture == null) {
            throw new NullPointerException("Texture cannot be null");
        }

        boundTextures[i] = texture;
        levels[i] = level;
    }

    public static void uploadSubTexture(int mipLevel, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, ByteBuffer buffer) {
        VulkanImage texture = boundTextures[activeTexture];

        if (texture == null) {
            throw new NullPointerException("Texture is null at index: " + activeTexture);
        }

        texture.uploadSubTextureAsync(mipLevel, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, buffer);
    }

    public static VulkanImage getTexture(String name) {
        return textureCache.get(name);
    }

    public static void setLightTexture(VulkanImage texture) {
        bindTexture(2, texture);
    }

    public static void setOverlayTexture(VulkanImage texture) {
        bindTexture(1, texture);
    }

    public static void setActiveTexture(int activeTexture) {
        if (activeTexture < 0 || activeTexture > 7) {
            throw new IllegalStateException(String.format("On Texture binding: index %d out of range [0, 7]", activeTexture));
        }

        VTextureSelector.activeTexture = activeTexture;
    }

    public static VulkanImage getBoundTexture(int i) { return boundTextures[i]; }

    public static VulkanImage getWhiteTexture() { return whiteTexture; }

    private static void inlineUploadSubTexture(int mipLevel, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, ByteBuffer buffer) {
        VulkanImage texture = boundTextures[activeTexture];

        if (texture == null) {
            throw new NullPointerException("Texture is null at index: " + activeTexture);
        }

        texture.uploadSubTextureAsync(mipLevel, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, buffer);
    }
}
