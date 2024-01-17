package net.vulkanmod.render.vertex;

import net.minecraft.client.renderer.RenderType;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.VRenderSystem;

import java.util.EnumSet;

public enum TerrainRenderType {
    SOLID(RenderType.solid(), 262144 /*BIG_BUFFER_SIZE*/),
    CUTOUT_MIPPED(RenderType.cutoutMipped(), 131072 /*SMALL_BUFFER_SIZE*/),
    CUTOUT(RenderType.cutout(), 131072 /*SMALL_BUFFER_SIZE*/),
    TRANSLUCENT(RenderType.translucent(), 262144 /*MEDIUM_BUFFER_SIZE*/),
    TRIPWIRE(RenderType.tripwire(), 262144 /*MEDIUM_BUFFER_SIZE*/);

    public static final TerrainRenderType[] VALUES = TerrainRenderType.values();

    public static final EnumSet<TerrainRenderType> COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, TRANSLUCENT);
    public static final EnumSet<TerrainRenderType> SEMI_COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
    public static final EnumSet<TerrainRenderType> ALL_RENDER_TYPES = EnumSet.allOf(TerrainRenderType.class);

    private final int maxSize;  //Not sure if this should be changed to UINT16_INDEX_MAX * vertexSize
    private final int initialSize; //Only used W/ Per RenderTy[e AreaBuffers

    TerrainRenderType(RenderType renderType, int initialSize) {

        this.maxSize = renderType.bufferSize();
        this.initialSize = initialSize;
    }

    public static EnumSet<TerrainRenderType> getActiveLayers() {
        return Initializer.CONFIG.fastLeavesFix ? SEMI_COMPACT_RENDER_TYPES : COMPACT_RENDER_TYPES;
    }

    public static TerrainRenderType get(String renderType) {
        switch (renderType) {
            case "solid":
                return SOLID;
            case "cutout_mipped":
                return CUTOUT_MIPPED;
            case "cutout":
                return CUTOUT;
            case "translucent":
                return TRANSLUCENT;
            case "tripwire":
                return TRIPWIRE;
            default:
                throw new IllegalStateException("Unexpected value: " + renderType);
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getInitialSize() {
        return initialSize;
    }
}
