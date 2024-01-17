package net.vulkanmod.render.vertex;

import net.minecraft.client.renderer.RenderType;
import net.vulkanmod.Initializer;

import java.util.EnumSet;

public enum TerrainRenderType {
    SOLID(RenderType.solid(), 262144),
    CUTOUT_MIPPED(RenderType.cutoutMipped(), 131072),
    CUTOUT(RenderType.cutout(), 131072),
    TRANSLUCENT(RenderType.translucent(), 262144),
    TRIPWIRE(RenderType.tripwire(), 262144);

    public static final TerrainRenderType[] VALUES = TerrainRenderType.values();

    public static final EnumSet<TerrainRenderType> COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, TRANSLUCENT);
    public static final EnumSet<TerrainRenderType> SEMI_COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
    public static final EnumSet<TerrainRenderType> ALL_RENDER_TYPES = EnumSet.allOf(TerrainRenderType.class);

    public final int maxSize;
    public final int initialSize;

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
