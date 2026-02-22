package com.venomgrave.hexvg.generator;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class SpikeGenerator {

    private SpikeGenerator() {}


    public static Runnable taskFor(World world, int chunkX, int chunkZ, com.venomgrave.hexvg.world.PlanetType type) {
        return () -> generate(world, chunkX, chunkZ, type, new Random(
                (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ world.getSeed() ^ 0xF00DL
        ));
    }


    public static void generate(World world, int chunkX, int chunkZ, com.venomgrave.hexvg.world.PlanetType type, Random rng) {
        switch (type) {
            case HOTH:
                generateHothSpikes(world, chunkX, chunkZ, rng);
                break;
            case TATOOINE:
                generateTatooineHoodoos(world, chunkX, chunkZ, rng);
                break;
            case MUSTAFAR:
                generateMustafarColumns(world, chunkX, chunkZ, rng);
                break;
            default:
                break;
        }
    }


    private static void generateHothSpikes(World world, int chunkX, int chunkZ, Random rng) {
        if (rng.nextInt(6) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int count = 1 + rng.nextInt(3);

        for (int i = 0; i < count; i++) {
            int sx = baseX + rng.nextInt(16);
            int sz = baseZ + rng.nextInt(16);
            int sy = findSurface(world, sx, sz);
            if (sy < world.getMinHeight() + 4) continue;

            int height = 4 + rng.nextInt(14);
            boolean packed = rng.nextBoolean();
            Material mat = packed ? Material.PACKED_ICE : Material.ICE;


            for (int dy = 0; dy < height; dy++) {
                double progress = (double) dy / height; // 0→1
                int radius = (int) Math.max(0, (1.5 * (1.0 - progress)));
                placeOrb(world, sx, sy + dy, sz, radius, mat);
            }


            if (sy + height < world.getMaxHeight()) {
                world.getBlockAt(sx, sy + height, sz).setType(Material.SNOW_BLOCK, false);
            }


            if (packed && rng.nextInt(3) == 0) {
                int branchY  = sy + height / 2 + rng.nextInt(Math.max(1, height / 3));
                int branchDir = rng.nextInt(4);
                int branchLen = 2 + rng.nextInt(3);
                int bsx = sx + (branchDir == 0 ? 1 : branchDir == 1 ? -1 : 0);
                int bsz = sz + (branchDir == 2 ? 1 : branchDir == 3 ? -1 : 0);
                for (int bdy = 0; bdy < branchLen; bdy++) {
                    int by = branchY + bdy;
                    if (by >= world.getMaxHeight()) break;
                    if (world.isChunkLoaded(bsx >> 4, bsz >> 4)) {
                        world.getBlockAt(bsx, by, bsz).setType(Material.PACKED_ICE, false);
                    }
                }
            }
        }
    }



    private static void generateTatooineHoodoos(World world, int chunkX, int chunkZ, Random rng) {

        if (rng.nextInt(8) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int sx = baseX + 2 + rng.nextInt(12);
        int sz = baseZ + 2 + rng.nextInt(12);
        int sy = findSurface(world, sx, sz);
        if (sy < world.getMinHeight() + 4) return;

        int height   = 6 + rng.nextInt(20);
        int baseRad  = 2 + rng.nextInt(3);

        for (int dy = 0; dy < height; dy++) {
            double progress = (double) dy / height;

            double radiusD;
            if (progress < 0.3) {
                radiusD = baseRad * (1.0 - progress * 0.5);
            } else if (progress < 0.7) {

                radiusD = baseRad * (0.6 - (progress - 0.3) * 0.5);
            } else {

                radiusD = baseRad * 0.4 + (progress - 0.7) * baseRad * 0.5;
            }
            int radius = Math.max(0, (int) radiusD);
            Material mat = (dy == height - 1) ? Material.SANDSTONE : Material.RED_SANDSTONE;
            placeOrb(world, sx, sy + dy, sz, radius, mat);
        }
    }


    private static void generateMustafarColumns(World world, int chunkX, int chunkZ, Random rng) {

        if (rng.nextInt(3) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int count = 1 + rng.nextInt(2);

        for (int i = 0; i < count; i++) {
            int sx = baseX + rng.nextInt(16);
            int sz = baseZ + rng.nextInt(16);


            int sy = findLavaOrSolid(world, sx, sz);
            if (sy < world.getMinHeight() + 4) continue;

            boolean obsidian = rng.nextInt(4) == 0; // 25% szans na obsydian
            Material mat = obsidian ? Material.OBSIDIAN : Material.BASALT;

            int height = obsidian ? (2 + rng.nextInt(8)) : (3 + rng.nextInt(12));
            int baseRad = obsidian ? 0 : 1;

            for (int dy = 0; dy <= height; dy++) {
                double progress = (double) dy / height;
                int radius = Math.max(0, baseRad - (int)(progress * (baseRad + 0.5)));
                placeOrb(world, sx, sy + dy, sz, radius, mat);
            }
        }
    }


    private static void placeOrb(World world, int cx, int cy, int cz, int radius, Material mat) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        double r2 = Math.max(0.1, (double) radius * radius);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx*dx + dy*dy + dz*dz > r2) continue;
                    int bx = cx + dx, by = cy + dy, bz = cz + dz;
                    if (by < minY || by > maxY) continue;
                    if (!world.isChunkLoaded(bx >> 4, bz >> 4)) continue;
                    world.getBlockAt(bx, by, bz).setType(mat, false);
                }
            }
        }

        if (cy >= minY && cy <= maxY && world.isChunkLoaded(cx >> 4, cz >> 4)) {
            world.getBlockAt(cx, cy, cz).setType(mat, false);
        }
    }

    private static int findSurface(World world, int x, int z) {
        int maxY = world.getMaxHeight() - 1;
        int minY = world.getMinHeight();
        for (int y = maxY; y >= minY; y--) {
            if (!world.isChunkLoaded(x >> 4, z >> 4)) return minY;
            Material m = world.getBlockAt(x, y, z).getType();
            if (!m.isAir()) return y + 1;
        }
        return minY;
    }

    private static int findLavaOrSolid(World world, int x, int z) {
        int maxY = world.getMaxHeight() - 1;
        int minY = world.getMinHeight();
        for (int y = maxY; y >= minY; y--) {
            if (!world.isChunkLoaded(x >> 4, z >> 4)) return minY;
            Material m = world.getBlockAt(x, y, z).getType();
            if (m == Material.LAVA || (!m.isAir() && m != Material.WATER)) return y + 1;
        }
        return minY;
    }
}