package net.vulkanmod.render.chunk.util;

import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Util {

    public static final Direction[] DIRECTIONS = Direction.values();

    public static final Direction[] XZ_DIRECTIONS = new Direction[]{
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST
    };

    public static long posLongHash(int x, int y, int z) {
        return (long) x | ((long) z << 16) | ((long) y << 32);
    }

    public static int flooredLog(int v) {
        assert v > 0;
        int log = 30;
        int t = 0x40000000;

        while (t > v) {
            t >>= 1;
            log--;
        }

        return log;
    }

    public static int align(int i, int alignment) {
        return i + alignment - (i % alignment);
    }

    public static ByteBuffer createCopy(ByteBuffer src) {
        if (src.isDirect()) {
            return src.duplicate();
        } else {
            return MemoryUtil.memSlice(src);
        }
    }

    public static Direction[] getXzDirections() {
        return XZ_DIRECTIONS;
    }
    }
