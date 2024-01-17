package net.vulkanmod.render.chunk.build;

import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class UploadBuffer {

    public final int indexCount;
    public final boolean autoIndices;
    public final boolean indexOnly;
    private ByteBuffer vertexBuffer; // Make it accessible for potential reuse
    private ByteBuffer indexBuffer; // Make it accessible for potential reuse

    public UploadBuffer(TerrainBufferBuilder.RenderedBuffer renderedBuffer) {
        TerrainBufferBuilder.DrawState drawState = renderedBuffer.drawState();
        this.indexCount = drawState.indexCount();
        this.autoIndices = drawState.sequentialIndex();
        this.indexOnly = drawState.indexOnly();

        if (!this.indexOnly) {
            // Acquire a direct buffer for reuse
            // (assuming Util.createCopy() allocates a direct buffer)
            this.vertexBuffer = Util.createCopy(renderedBuffer.vertexBuffer());
        }

        if (!drawState.sequentialIndex()) {
            // Acquire a direct buffer for reuse
            this.indexBuffer = Util.createCopy(renderedBuffer.indexBuffer());
        }
    }

    public int indexCount() {
        return indexCount;
    }

    public ByteBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public ByteBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public void release() {
        // Relinquish direct buffers for reuse
        this.vertexBuffer = null;
        this.indexBuffer = null;
        // MemoryUtil.memFree() calls are removed for reuse
    }
}
