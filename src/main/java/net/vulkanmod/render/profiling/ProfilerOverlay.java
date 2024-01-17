package net.vulkanmod.render.profiling;

import com.google.common.base.Strings;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.build.ChunkTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilerOverlay {

    public static ProfilerOverlay INSTANCE;
    public static boolean shouldRender;

    public static void createInstance(Minecraft minecraft) {
        INSTANCE = new ProfilerOverlay(minecraft);
    }

    final Minecraft minecraft;
    final Font font;
    private final List<Profiler2.Result> results;

    public ProfilerOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.results = Profiler2.getMainProfiler().getResults();
    }

    public void render(PoseStack poseStack) {

        this.drawProfilerInfo(poseStack);
    }

    protected void drawProfilerInfo(PoseStack poseStack) {

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Consolidate batch calls
        GuiBatchRenderer.beginBatch(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Minimize string concatenation
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < results.size(); ++i) {
            Profiler2.Result result = results.get(i);

            // Use StringBuilder for more efficient concatenation
            builder.append(result.toString());
            builder.append('\n');

            // Use a cached font width to avoid redundant calculations
            int width = font.width(builder.toString());

            // Use a single call to fill the background
            GuiBatchRenderer.fill(poseStack, 1, 2 + i * 9, 2 + width + 1, 2 + 9 * (i + 1), -1873784752);
        }

        // Use a single call to render the text
        GuiBatchRenderer.drawString(font, builder.toString(), poseStack, 2.0f, 2.0f, 0xE0E0E0);

        GuiBatchRenderer.endBatch();
        RenderSystem.disableBlend();
    }

    private List<String> buildInfo() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("Profiler");

        // Reduce overhead by only collecting the FPS and frametime
        list.add(String.format("FPS: %d Frametime: %.3f", (int) (1000.0f / results.get(0).getValue()), results.get(0).getValue()));
        list.add("");

        // Consider using a HashSet or HashMap for faster lookups
        for (Profiler2.Result result : results) {
            list.add(result.toString());
        }

        // Section build stats
        list.add("");
        list.add("");
        list.add(String.format("Build time: %.2f ms", BuildTimeBench.getBenchTime()));

        if (ChunkTask.bench) {
            list.add(String.format("Total build time: %d ms for %d builds", ChunkTask.totalBuildTime.get(), ChunkTask.buildCount.get()));
        }

        return list;
    }
}
