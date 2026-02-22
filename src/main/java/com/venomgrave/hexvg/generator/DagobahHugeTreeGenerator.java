package com.venomgrave.hexvg.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public final class DagobahHugeTreeGenerator {

    private DagobahHugeTreeGenerator() {}

    public static final int DEFAULT_RARITY = 6;


    public static Runnable taskFor(World world, int chunkX, int chunkZ, Random rng) {
        final long seed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ world.getSeed() ^ 0xDAB0B0L;
        final int bx = chunkX * 16 + rng.nextInt(16);
        final int bz = chunkZ * 16 + rng.nextInt(16);

        return () -> {
            if (!world.isChunkLoaded(chunkX, chunkZ)) return;
            int by = world.getHighestBlockYAt(bx, bz);
            // Upewnij się że stoimy na solid (nie woda)
            while (by > world.getMinHeight() &&
                    (world.getBlockAt(bx, by, bz).getType() == Material.AIR ||
                            world.getBlockAt(bx, by, bz).getType() == Material.WATER)) {
                by--;
            }
            generate(world, bx, by + 1, bz, new Random(seed));
        };
    }


    public static void generate(World world, int x, int y, int z, Random rng) {
        int height = 10 + rng.nextInt(8);   // 10–17 bloków
        int crownR = 4 + rng.nextInt(3);    // promień korony 4–6

        placeTrunk(world, x, y, z, height, rng);
        placeAerialRoots(world, x, y, z, height, rng);
        placeCrown(world, x, y + height, z, crownR, rng);
        placeVines(world, x, y + height, z, crownR, rng);
    }


    private static void placeTrunk(World world, int x, int y, int z, int height, Random rng) {
        int cx = x, cz = z;
        for (int dy = 0; dy < height; dy++) {
            placeLog(world, cx, y + dy, cz);

            if (dy < height / 3) {
                placeLog(world, cx + 1, y + dy, cz);
                placeLog(world, cx, y + dy, cz + 1);
                placeLog(world, cx + 1, y + dy, cz + 1);
            }

            if (rng.nextInt(5) == 0) {
                placeSafe(world, cx + (rng.nextBoolean() ? 1 : -1), y + dy, cz, Material.MOSS_BLOCK);
            }

            if (dy > 3 && rng.nextInt(4) == 0) {
                cx += rng.nextInt(3) - 1;
                cz += rng.nextInt(3) - 1;
            }
        }
    }


    private static void placeAerialRoots(World world, int x, int y, int z, int height, Random rng) {
        int rootCount = 3 + rng.nextInt(5);
        for (int i = 0; i < rootCount; i++) {
            int rx = x + rng.nextInt(7) - 3;
            int rz = z + rng.nextInt(7) - 3;
            int startY = y + 2 + rng.nextInt(height / 2);

            for (int ry = startY; ry > world.getMinHeight(); ry--) {
                if (!world.isChunkLoaded(rx >> 4, rz >> 4)) break;
                Block b = world.getBlockAt(rx, ry, rz);
                if (b.getType() == Material.AIR || b.getType() == Material.WATER) {
                    b.setType(Material.JUNGLE_LOG, false);
                } else {
                    break; // dotknął ziemi
                }
            }
        }
    }


    private static void placeCrown(World world, int cx, int cy, int cz, int radius, Random rng) {
        int lobeCount = 3 + rng.nextInt(4);
        for (int lobe = 0; lobe < lobeCount; lobe++) {
            int lx = cx + rng.nextInt(radius) - radius / 2;
            int ly = cy + rng.nextInt(3) - 1;
            int lz = cz + rng.nextInt(radius) - radius / 2;
            int lr = 2 + rng.nextInt(3);

            for (int dx = -lr; dx <= lr; dx++) {
                for (int dy = -lr; dy <= lr; dy++) {
                    for (int dz = -lr; dz <= lr; dz++) {
                        if (dx * dx + dy * dy + dz * dz <= lr * lr + rng.nextInt(2)) {
                            placeSafe(world, lx + dx, ly + dy, lz + dz, Material.JUNGLE_LEAVES);
                        }
                    }
                }
            }
        }
    }


    private static void placeVines(World world, int cx, int cy, int cz, int radius, Random rng) {
        int vineCount = 6 + rng.nextInt(10);
        for (int i = 0; i < vineCount; i++) {
            int vx = cx + rng.nextInt(radius * 2) - radius;
            int vz = cz + rng.nextInt(radius * 2) - radius;
            int length = 2 + rng.nextInt(6);

            int startY = cy;
            for (int y = cy; y > cy - radius - 2; y--) {
                if (!world.isChunkLoaded(vx >> 4, vz >> 4)) break;
                if (world.getBlockAt(vx, y, vz).getType() == Material.JUNGLE_LEAVES) {
                    startY = y - 1;
                    break;
                }
            }

            for (int dy = 0; dy < length; dy++) {
                int vy = startY - dy;
                if (!world.isChunkLoaded(vx >> 4, vz >> 4)) break;
                Block b = world.getBlockAt(vx, vy, vz);
                if (b.getType() == Material.AIR) {
                    b.setType(Material.VINE, false);
                } else {
                    break;
                }
            }
        }
    }


    private static void placeLog(World world, int x, int y, int z) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
        world.getBlockAt(x, y, z).setType(Material.JUNGLE_LOG, false);
    }

    private static void placeSafe(World world, int x, int y, int z, Material mat) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
        Block b = world.getBlockAt(x, y, z);
        if (b.getType().isAir() || b.getType() == Material.WATER) {
            b.setType(mat, false);
        }
    }
}