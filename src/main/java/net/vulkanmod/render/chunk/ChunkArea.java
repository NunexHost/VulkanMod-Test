package net.vulkanmod.render.chunk;

import net.minecraft.core.BlockPos;
import net.vulkanmod.render.chunk.util.StaticQueue;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;
import org.joml.Vector3i;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public record ChunkArea(int index, byte[] inFrustum, Vector3i position, DrawBuffers drawBuffers, EnumMap<TerrainRenderType, StaticQueue<DrawBuffers.DrawParameters>> sectionQueues) {


    public ChunkArea(int i, Vector3i origin, int minHeight) {
        this(i, new byte[64], origin, new DrawBuffers(i, origin, minHeight), new EnumMap<>(TerrainRenderType.class));
        for (TerrainRenderType renderType : TerrainRenderType.VALUES) {
            sectionQueues.put(renderType, new StaticQueue<>(512));
        }
    }

    public void updateFrustum(VFrustum frustum) {
        //TODO: maybe move to an aux class
        frustum.intersectChunk(this.position.x(), this.position.y(), this.position.z(), this.inFrustum);
    }

    public byte getFrustumIndex(BlockPos pos) {
        return getFrustumIndex(pos.getX(), pos.getY(), pos.getZ());
    }

    public byte getFrustumIndex(int x, int y, int z) {
        int dx = x - this.position.x;
        int dy = y - this.position.y;
        int dz = z - this.position.z;

        int i = (dx >> 6 << 5)
                + (dy >> 6 << 4)
                + (dz >> 6 << 3);

        return this.inFrustum[i];
    }

    public byte inFrustum(byte i) {
        return this.inFrustum[i];
    }

    public DrawBuffers getDrawBuffers() {
        if(!this.drawBuffers.isAllocated())
            this.drawBuffers.allocateBuffers();

        return this.drawBuffers;
    }

    public void addSections(RenderSection section) {
        for(var t : section.getCompiledSection().renderTypes) {
            this.sectionQueues.get(t).add(section.getDrawParameters(t));
        }
    }

    public void resetQueue() {
        this.sectionQueues.forEach((renderType, drawParameters) -> drawParameters.clear());
    }

    public void setPosition(int x, int y, int z) {
        this.position.set(x, y, z);
    }

    public void releaseBuffers() {
        this.drawBuffers.releaseBuffers();
    }
}
