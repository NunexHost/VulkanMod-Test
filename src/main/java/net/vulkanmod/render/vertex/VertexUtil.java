package net.vulkanmod.render.vertex;

import net.vulkanmod.vulkan.util.ColorUtil;

public class VertexUtil {

    private static final float FLOAT_TO_BYTE_NORM = 1.0f / 127.0f;
    private static final float FLOAT_TO_BYTE_COLOR = 1.0f / 255.0f;

    public static int packColor(float r, float g, float b, float a) {
        return ColorUtil.packColorIntRGBA(r, g, b, a);
    }

    public static int packNormal(float x, float y, float z) {
        return (int)(x * FLOAT_TO_BYTE_NORM) |
                (int)(y * FLOAT_TO_BYTE_NORM) << 8 |
                (int)(z * FLOAT_TO_BYTE_NORM) << 16;
    }

    public static float unpackColor(int i, int shift) {
        return (i >> shift) & 0xFF;
    }

    public static float unpackNormal(int i) {
        return (i & 0xFF) * FLOAT_TO_BYTE_NORM;
    }

}
