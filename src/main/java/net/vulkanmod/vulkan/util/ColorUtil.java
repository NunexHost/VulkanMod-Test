package net.vulkanmod.vulkan.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ColorUtil {

    static ColorConsumer colorConsumer = new DefaultColorConsumer();

    public static void useGammaCorrection(boolean b) {
        if (b) {
            colorConsumer = new GammaColorConsumer();
        } else {
            colorConsumer = new DefaultColorConsumer();
        }
    }

    public static int packColorIntRGBA(float r, float g, float b, float a) {
        return Float.floatToIntBits(a) << 24 |
                Float.floatToIntBits(b) << 16 |
                Float.floatToIntBits(g) << 8 |
                Float.floatToIntBits(r);
    }

    public static int BGRAtoRGBA(int v) {
        return (v & 0xFF000000) |
                ((v & 0xFF0000) >> 16) |
                ((v & 0xFF00) >> 8) |
                (v & 0xFF);
    }

    public static float gamma(float f) {
        // Pre-calculate the table of powers of 2.2
        static float[] pow22 = new float[256];
        for (int i = 0; i < 256; i++) {
            pow22[i] = (float) Math.pow(i / 255.0f, 2.2f);
        }

        // Use the table to calculate the gamma value
        return pow22[(int) (f * 255.0f)];
    }

    public static void setRGBA_Buffer(MappedBuffer buffer, float r, float g, float b, float a) {
        // Use Unsafe to write the values directly to the buffer
        int offset = 0;
        Unsafe.putFloat(buffer.memory, offset, r);
        offset += 4;
        Unsafe.putFloat(buffer.memory, offset, g);
        offset += 4;
        Unsafe.putFloat(buffer.memory, offset, b);
        offset += 4;
        Unsafe.putFloat(buffer.memory, offset, a);
    }

    // Alternative implementation of `setRGBA_Buffer()` that uses a simpler `ColorConsumer` interface

    public static void setRGBA_Buffer(ByteBuffer buffer, float r, float g, float b, float a) {
        // Use the `ColorConsumer` interface to write the values to the buffer
        colorConsumer.setRGBA(buffer, r, g, b, a);
    }

    public static interface ColorConsumer {

        void setRGBA(ByteBuffer buffer, float r, float g, float b, float a);
    }

    public static class DefaultColorConsumer implements ColorConsumer {

        @Override
        public void setRGBA(ByteBuffer buffer, float r, float g, float b, float a) {
            buffer.putFloat(0, r);
            buffer.putFloat(4, g);
            buffer.putFloat(8, b);
            buffer.putFloat(12, a);
        }
    }

    public static class GammaColorConsumer implements ColorConsumer {

        @Override
        public void setRGBA(ByteBuffer buffer, float r, float g, float b, float a) {
            r = gamma(r);
            g = gamma(g);
            b = gamma(b);
            buffer.putFloat(0, r);
            buffer.putFloat(4, g);
            buffer.putFloat(8, b);
            buffer.putFloat(12, a);
        }
    }
}
