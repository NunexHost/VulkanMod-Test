package net.vulkanmod.render.chunk.build;

import net.minecraft.client.renderer.RenderType;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.TerrainRenderType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ThreadBuilderPack {

    private static Function<TerrainRenderType, TerrainBufferBuilder> terrainBuilderConstructor = renderType -> new TerrainBufferBuilder(renderType.maxSize);

    public static void defaultTerrainBuilderConstructor() {
        terrainBuilderConstructor = renderType -> new TerrainBufferBuilder(renderType.maxSize);
    }

    public static void setTerrainBuilderConstructor(Function<TerrainRenderType, TerrainBufferBuilder> constructor) {
        terrainBuilderConstructor = constructor;
    }

    private final HashMap<TerrainRenderType, TerrainBufferBuilder> builders = new HashMap<>();

    public ThreadBuilderPack() {
        for (TerrainRenderType renderType : TerrainRenderType.getActiveLayers()) {
            builders.put(renderType, terrainBuilderConstructor.apply(renderType));
        }
    }

    public TerrainBufferBuilder builder(TerrainRenderType renderType) {
        return builders.computeIfAbsent(renderType, terrainBuilderConstructor);
    }

    public void clearAll() {
        builders.values().forEach(TerrainBufferBuilder::clear);
    }

    public void discardAll() {
        builders.values().forEach(TerrainBufferBuilder::discard);
    }
}
