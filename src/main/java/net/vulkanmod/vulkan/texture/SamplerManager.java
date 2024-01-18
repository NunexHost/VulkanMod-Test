package net.vulkanmod.vulkan.texture;

import it.unimi.dsi.fastutil.shorts.Short2LongMap;
import it.unimi.dsi.fastutil.shorts.Short2LongOpenHashMap;
import net.vulkanmod.vulkan.DeviceManager;
import org.apache.commons.lang3.Validate;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkSamplerReductionModeCreateInfo;

import java.nio.LongBuffer;

import static net.vulkanmod.vulkan.Vulkan.getDevice;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_SAMPLER_REDUCTION_MODE_MAX;
import static org.lwjgl.vulkan.VK12.VK_SAMPLER_REDUCTION_MODE_MIN;

public abstract class SamplerManager {

    static final float MIP_BIAS = -0.5f;

    static final Short2LongMap SAMPLERS = new Short2LongOpenHashMap();

    public static long getTextureSampler(byte mipLevels, byte flags) {
        short key = (short) (flags | (mipLevels << 8));
        long sampler = SAMPLERS.getOrDefault(key, 0L);

        if (sampler == 0L) {
            sampler = createTextureSampler(mipLevels, flags);
            SAMPLERS.put(key, sampler);
        }

        return sampler;
    }

    private static long createTextureSampler(byte mipLevels, byte flags) {
        Validate.isTrue((flags & (REDUCTION_MIN_BIT | REDUCTION_MAX_BIT)) != (REDUCTION_MIN_BIT | REDUCTION_MAX_BIT));

        try (MemoryStack stack = stackPush()) {

            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);

            samplerInfo.magFilter((flags & LINEAR_FILTERING_BIT) != 0 ? VK_FILTER_LINEAR : VK_FILTER_NEAREST);
            samplerInfo.minFilter((flags & LINEAR_FILTERING_BIT) != 0 ? VK_FILTER_LINEAR : VK_FILTER_NEAREST);

            samplerInfo.addressModeU((flags & CLAMP_BIT) != 0 ? VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV((flags & CLAMP_BIT) != 0 ? VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW((flags & CLAMP_BIT) != 0 ? VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE : VK_SAMPLER_ADDRESS_MODE_REPEAT);

            samplerInfo.mipmapMode((flags & USE_MIPMAPS_BIT) != 0 ? VK_SAMPLER_MIPMAP_MODE_LINEAR : VK_SAMPLER_MIPMAP_MODE_NEAREST);
            samplerInfo.maxLod(mipLevels);
            samplerInfo.minLod(0.0F);
            samplerInfo.mipLodBias(MIP_BIAS);

            // Reduction Mode
            if ((flags & (REDUCTION_MIN_BIT | REDUCTION_MAX_BIT)) != 0) {
                VkSamplerReductionModeCreateInfo reductionModeInfo = VkSamplerReductionModeCreateInfo.calloc(stack);
                reductionModeInfo.sType$Default();
                reductionModeInfo.reductionMode((flags & REDUCTION_MAX_BIT) != 0 ? VK_SAMPLER_REDUCTION_MODE_MAX : VK_SAMPLER_REDUCTION_MODE_MIN);
                samplerInfo.pNext(reductionModeInfo.address());
            }

            LongBuffer pTextureSampler = stack.mallocLong(1);

            if (vkCreateSampler(getDevice(), samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create texture sampler");
            }

            return pTextureSampler.get(0);
        }
        
    }

    public static final byte LINEAR_FILTERING_BIT = 1;
    public static final byte CLAMP_BIT = 2;
    public static final byte USE_MIPMAPS_BIT = 4;
    public static final byte REDUCTION_MIN_BIT = 8;
    public static final byte REDUCTION_MAX_BIT = 16;
}
