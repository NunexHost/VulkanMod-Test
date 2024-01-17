package net.vulkanmod.render.util;

import it.unimi.dsi.fastutil.ints.IntComparator;

public class MathUtil {

    public static float clamp(float min, float max, float x) {
        return Math.min(Math.max(x, min), max);
    }

    public static int clamp(int min, int max, int x) {
        return Math.min(Math.max(x, min), max);
    }

    public static float saturate(float x) {
        return clamp(0.0f, 1.0f, x);
    }

    public static float lerp(float v0, float v1, float t) {
        return v0 + t * (v1 - v0);
    }

    public static float clamp(float min, float max, float x) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }

        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }

    public static int clamp(int min, int max, int x) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }

        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }

    public static float saturate(float x) {
        return clamp(0.0f, 1.0f, x);
    }

    public static float lerp(float v0, float v1, float t) {
        return v0 + t * (v1 - v0);
    }
}
