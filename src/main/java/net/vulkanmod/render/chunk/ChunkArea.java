package net.vulkanmod.render.chunk;

import net.minecraft.core.BlockPos;
import net.vulkanmod.render.chunk.util.StaticQueue;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.joml.FrustumIntersection;
import org.joml.Vector3i;

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
        int frustumResult = frustum.cubeInFrustum(this.position.x(), this.position.y(), this.position.z(),
                this.position.x() + (8 << 4) , this.position.y() + (8 << 4), this.position.z() + (8 << 4));

        //Inner cubes
        if (frustumResult == FrustumIntersection.INTERSECT) {
            int width = 8 << 4;
            int l = width >> 1;

            //Use a quadtree to accelerate frustum culling
            Quadtree quadtree = new Quadtree(this.position, width, 2);
            quadtree.update(frustum);

            //Iterate over all cubes in the quadtree
            for (Quadtree.Node node : quadtree.getLeaves()) {
                int idx = node.getIndex();

                //Only update cubes that are visible
                if (node.isVisible()) {
                    this.inFrustum[idx] = (byte) frustumResult;
                }
            }
        } else {
            Arrays.fill(inFrustum, (byte) frustumResult);
        }

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

        int xSub = (dx >> 3) & 0b100;
        int ySub = (dy >> 4) & 0b10;
        int zSub = (dz >> 5) & 0b1;

        return (byte) (i + xSub + ySub + zSub);
    }

    public byte inFrustum(byte i) {
        return this.inFrustum[i];
    }

    public DrawBuffers getDrawBuffers() {
        if(!this.drawBuffers.isAllocated())
            drawBuffers.allocateBuffers();

        return this.drawBuffers;
    }

    private void allocateDrawBuffers() {
        this.drawBuffers = new DrawBuffers(this.index, this.position);
    }

    public void addSections(RenderSection section) {
        for(var t : section.getCompiledSection().renderTypes) {
            this.sectionQueues.get(t).add(section.getDrawParameters(t));
        }
    }

    public void resetQueue() {
        this.sectionQueues.forEach((renderType, drawParameters) -> drawParameters.clear());
        }
// Chunk Loading and Unloading

// Implement a LOD (Level of Detail) system

public enum LOD {
    LOW(0),
    MEDIUM(1),
    HIGH(2);

    public int getLodLevel() {
        return ordinal();
    }
}

public class ChunkArea {

    //...

    private LOD lod;

    public ChunkArea(int i, Vector3i origin, int minHeight) {
        //...
        this.lod = LOD.MEDIUM;
    }

    //...

    public void updateLod(float cameraDistance) {
        float lodDistance = 1000.0f;

        switch (this.lod) {
            case LOW:
                if (cameraDistance > lodDistance * 2) {
                    this.lod = LOD.HIGH;
                }
                break;
            case MEDIUM:
                if (cameraDistance > lodDistance) {
                    this.lod = LOD.LOW;
                } else if (cameraDistance > lodDistance * 0.5f) {
                    this.lod = LOD.HIGH;
                }
                break;
            case HIGH:
                if (cameraDistance < lodDistance * 0.5f) {
                    this.lod = LOD.MEDIUM;
                }
                break;
        }
    }

    //...

    // Prioritize loading and updating of nearby chunks

    public void loadChunk() {
        // Only load chunks that are within a certain distance from the player
        float cameraDistance = Math.sqrt(Math.pow(camera.x, 2) + Math.pow(camera.y, 2) + Math.pow(camera.z, 2));
        if (cameraDistance < this.lod.getLodLevel() * 2) {
            this.load();
        }
    }
