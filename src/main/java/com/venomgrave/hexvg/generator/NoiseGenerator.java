package com.venomgrave.hexvg.generator;

import java.util.Random;


public class NoiseGenerator {

    private final long seed;
    private final double[] perm;

    public NoiseGenerator(long seed) {
        this.seed = seed;
        this.perm = buildPermTable(seed);
    }


    public double noise(int x, int z, int octaves, int scale) {
        double result = 0;
        double amplitude = 1.0;
        double frequency = 1.0 / scale;
        double max = 0;

        for (int i = 0; i < octaves; i++) {
            result += smooth(x * frequency, z * frequency) * amplitude;
            max += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }

        return (result / max + 1.0) * 0.5; // Normalise to [0, 1]
    }


    public double noise(int x, int y, int z, int octaves, int scale) {
        double result = 0;
        double amplitude = 1.0;
        double frequency = 1.0 / scale;
        double max = 0;

        for (int i = 0; i < octaves; i++) {
            result += smooth3(x * frequency, y * frequency, z * frequency) * amplitude;
            max += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }

        return (result / max + 1.0) * 0.5;
    }


    private double smooth(double x, double z) {
        int xi = (int) Math.floor(x) & 255;
        int zi = (int) Math.floor(z) & 255;
        double xf = x - Math.floor(x);
        double zf = z - Math.floor(z);
        double u  = fade(xf);
        double w  = fade(zf);

        double a  = perm[xi]        + zi;
        double b  = perm[xi + 1]    + zi;

        return lerp(w,
                lerp(u, grad2(perm[(int)a],     xf,      zf),
                        grad2(perm[(int)b],     xf - 1,  zf)),
                lerp(u, grad2(perm[(int)(a+1)], xf,      zf - 1),
                        grad2(perm[(int)(b+1)], xf - 1,  zf - 1)));
    }

    private double smooth3(double x, double y, double z) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double zf = z - Math.floor(z);
        double u = fade(xf), v = fade(yf), w = fade(zf);

        int a  = (int) perm[xi]     + yi;
        int b  = (int) perm[xi + 1] + yi;
        int aa = (int) perm[a]      + zi;
        int ab = (int) perm[a + 1]  + zi;
        int ba = (int) perm[b]      + zi;
        int bb = (int) perm[b + 1]  + zi;

        return lerp(w,
                lerp(v,
                        lerp(u, grad3(perm[aa],   xf,   yf,   zf),
                                grad3(perm[ba],   xf-1, yf,   zf)),
                        lerp(u, grad3(perm[ab],   xf,   yf-1, zf),
                                grad3(perm[bb],   xf-1, yf-1, zf))),
                lerp(v,
                        lerp(u, grad3(perm[aa+1], xf,   yf,   zf-1),
                                grad3(perm[ba+1], xf-1, yf,   zf-1)),
                        lerp(u, grad3(perm[ab+1], xf,   yf-1, zf-1),
                                grad3(perm[bb+1], xf-1, yf-1, zf-1))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad2(double hash, double x, double z) {
        int h = (int) hash & 3;
        double u = h < 2 ? x : z;
        double v = h < 2 ? z : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double grad3(double hash, double x, double y, double z) {
        int h = (int) hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static double[] buildPermTable(long seed) {
        Random rng = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 255; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = p[i]; p[i] = p[j]; p[j] = tmp;
        }
        double[] perm = new double[512];
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
        return perm;
    }
}