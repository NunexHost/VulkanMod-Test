package net.vulkanmod.render.chunk;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.build.ThreadBuilderPack;
import net.vulkanmod.render.vertex.CustomVertexFormat;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.SPIRVUtils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class TerrainShaderManager {

    private static final String basePath = String.format("basic/%s/%s", "terrain", "terrain");
    public static VertexFormat TERRAIN_VERTEX_FORMAT;
    private static final Map<String, SPIRVUtils.SPIRV> shaderCache = new HashMap<>();

    static GraphicsPipeline terrainShaderEarlyZ;
    static GraphicsPipeline terrainShader;

    public static void init() {
        TERRAIN_VERTEX_FORMAT = CustomVertexFormat.COMPRESSED_TERRAIN; // Set the global format directly
        createBasicPipelines();
        setDefaultShader();
        ThreadBuilderPack.defaultTerrainBuilderConstructor();
    }

    public static void setDefaultShader() {
        setShaderGetter(renderType -> terrainShader);
    }

    private static void createBasicPipelines() {
        terrainShaderEarlyZ = createPipeline("terrain_Z");
        terrainShader = createPipeline("terrain");
    }

    private static GraphicsPipeline createPipeline(String fragPath) {
        String pathF = String.format("%s%s.fsh", basePath, fragPath);

        Pipeline.Builder pipelineBuilder = new Pipeline.Builder(CustomVertexFormat.COMPRESSED_TERRAIN, basePath);
        pipelineBuilder.parseBindingsJSON();

        // **Cache the compiled shader code**
        SPIRVUtils.SPIRV fragShaderSPIRV = getShader(pathF);

        // **Create the pipeline**
        pipelineBuilder.compileShaders(); // Assuming this handles the cached SPIRV data internally
        return pipelineBuilder.createGraphicsPipeline();
    }

    private static SPIRVUtils.SPIRV getShader(String fragPath) {
        if (!shaderCache.containsKey(fragPath)) {
            String path = Paths.get(Initializer.getAssetsPath(), fragPath).toString(); // Use the correct method to access the assets path
            SPIRVUtils.SPIRV fragShaderSPIRV = SPIRVUtils.compileShader(path, SPIRVUtils.ShaderKind.FRAGMENT_SHADER, SPIRVUtils.ShaderKind.VERTEX_SHADER); // Provide an empty vertex shader kind
            if (fragShaderSPIRV == null) {
                throw new RuntimeException("Failed to compile shader: " + path);
            }
            shaderCache.put(fragPath, fragShaderSPIRV);
        }
        return shaderCache.get(fragPath);
    }

    public static GraphicsPipeline getTerrainShader(TerrainRenderType renderType) {
        return switch (renderType) {
            case SOLID, TRANSLUCENT, TRIPWIRE -> terrainShaderEarlyZ;
            case CUTOUT_MIPPED, CUTOUT -> terrainShader;
        };
    }

    public static void setShaderGetter(Function<TerrainRenderType, GraphicsPipeline> consumer) {
        TerrainShaderManager.shaderGetter = consumer;
    }

    public static void destroyPipelines() {
        terrainShaderEarlyZ.cleanUp();
        terrainShader.cleanUp();
    }

    private static Function<TerrainRenderType, GraphicsPipeline> shaderGetter = renderType -> terrainShader;

}
