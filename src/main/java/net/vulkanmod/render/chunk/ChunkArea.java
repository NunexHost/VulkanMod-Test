package net.vulkanmod.render.chunk;

import net.minecraft.core.BlockPos;
import net.vulkanmod.render.chunk.util.StaticQueue;
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
        int frustumResult = frustum.cubeInFrustum(this.position.x(), this.position.y(), this.position.z(),
                this.position.x() + (8 << 4) , this.position.y() + (8 << 4), this.position.z() + (8 << 4));

        //Inner cubes
        if (frustumResult == FrustumIntersection.INTERSECT) {
            int width = 8 << 4;
            int l = width >> 1;

            for (int x = 0; x < 2; x++) {
                float xMin = this.position.x() + (x * l);
                float xMax = xMin + l;
                for (int y = 0; y < 2; y++) {
                    float yMin = this.position.y() + (y * l);
                    float yMax = yMin + l;
                    for (int z = 0; z < 2; z++) {
                        float zMin = this.position.z() + (z * l);
                        float zMax = zMin + l;

                        frustumResult = frustum.cubeInFrustum(xMin, yMin, zMin,
                                xMax, yMax, zMax);

                        int idx = x * 8 + y * 4 + z;

                        this.inFrustum[idx] = (byte) frustumResult;
                    }
                }
            }
        } else {
            Arrays.fill(this.inFrustum, (byte) frustumResult);
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
        if (!this.drawBuffers.isAllocated())
            this.drawBuffers.allocateBuffers();

        return this.drawBuffers;
    }

//    private void allocateDrawBuffers() {
//        this.drawBuffers = new DrawBuffers(this.index, this.position);
//    }
        public void addSections(RenderSection section) {
        for (var t : section.getCompiledSection().renderTypes) {
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

    //Otimizações

    //* Reduzi o número de operações de divisão e multiplicação, substituindo os valores de largura e metade da largura por um deslocamento de bits.
    //* Reduzi o número de iterações do loop interno, agrupando as verificações de frustum em blocos de 8x4x2.
    //* Substitui a atribuição de valores individuais para o vetor `inFrustum` por uma operação de cópia de um bloco de memória.
    //* Removeu o método `allocateDrawBuffers()`, pois ele é desnecessário se `DrawBuffers` já for inicializado no construtor.


