package com.venomgrave.hexvg.populator.mustafar;

import com.venomgrave.hexvg.populator.AbstractPopulator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MustafarTemplePopulator extends AbstractPopulator {

    public static final int DEFAULT_RARITY = 500;

    private static final Material[] WALL = {
            Material.NETHER_BRICKS,
            Material.CRACKED_NETHER_BRICKS,
            Material.CHISELED_NETHER_BRICKS,
            Material.NETHER_BRICK_FENCE,
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random,
                         int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(DEFAULT_RARITY) != 0) return;

        int bx = chunkX * 16 + 4 + random.nextInt(8);
        int bz = chunkZ * 16 + 4 + random.nextInt(8);
        int by = findSolidY(bx, bz, region) - 1;

        if (!isSafe(region, bx, by, bz)) return;
        if (region.getType(bx, by, bz) == Material.LAVA) return;

        buildTemple(region, bx, by, bz, random);
    }


    private static void buildTemple(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        buildFoundation(region, ox, oy, oz, rng);
        buildPyramid(region, ox, oy, oz, rng);
        buildEntrance(region, ox, oy, oz, rng);
        buildInterior(region, ox, oy, oz, rng);
        addDecay(region, ox, oy, oz, rng);
        addLava(region, ox, oy, oz, rng);
    }


    private static void buildFoundation(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        for (int dx = -1; dx <= 19; dx++) {
            for (int dz = -1; dz <= 19; dz++) {
                set(region, ox + dx, oy - 1, oz + dz, Material.NETHER_BRICKS);
                set(region, ox + dx, oy - 2, oz + dz, Material.BLACKSTONE);
            }
        }
    }


    private static void buildPyramid(LimitedRegion region, int ox, int oy, int oz, Random rng) {

        fillLayer(region, ox, oy, oz, 19, 3, rng);

        fillLayer(region, ox + 2, oy + 3, oz + 2, 15, 3, rng);

        fillLayer(region, ox + 4, oy + 6, oz + 4, 11, 3, rng);

        fillLayer(region, ox + 6, oy + 9, oz + 6, 7, 2, rng);

        for (int dx = 7; dx <= 11; dx++) {
            for (int dz = 7; dz <= 11; dz++) {
                set(region, ox + dx, oy + 11, oz + dz, Material.OBSIDIAN);
            }
        }
        set(region, ox + 9, oy + 12, oz + 9, Material.CRYING_OBSIDIAN);
        set(region, ox + 9, oy + 13, oz + 9, Material.SOUL_LANTERN);
    }

    private static void fillLayer(LimitedRegion region, int sx, int sy, int sz,
                                  int size, int height, Random rng) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    boolean isWall = dx == 0 || dx == size - 1 || dz == 0 || dz == size - 1
                            || dy == height - 1;
                    if (isWall) {
                        set(region, sx + dx, sy + dy, sz + dz,
                                rng.nextInt(6) == 0 ? Material.CRACKED_NETHER_BRICKS
                                        : Material.NETHER_BRICKS);
                    } else {
                        set(region, sx + dx, sy + dy, sz + dz, Material.NETHER_BRICKS);
                    }
                }
            }
        }
    }


    private static void buildEntrance(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int gx = ox + 9; // środe
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 3; dy++) {
                clear(region, gx + dx, oy + dy, oz);
            }
        }

        for (int dy = 0; dy <= 4; dy++) {
            set(region, gx - 2, oy + dy, oz - 1, Material.OBSIDIAN);
            set(region, gx + 2, oy + dy, oz - 1, Material.OBSIDIAN);
        }

        set(region, gx - 1, oy + 4, oz, Material.CHISELED_NETHER_BRICKS);
        set(region, gx,     oy + 4, oz, Material.CHISELED_NETHER_BRICKS);
        set(region, gx + 1, oy + 4, oz, Material.CHISELED_NETHER_BRICKS);
    }


    private static void buildInterior(LimitedRegion region, int ox, int oy, int oz, Random rng) {

        for (int dx = 5; dx <= 13; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = 5; dz <= 13; dz++) {
                    if (dy == 0) {
                        set(region, ox + dx, oy + dy, oz + dz, Material.SOUL_SAND);
                    } else {
                        clear(region, ox + dx, oy + dy, oz + dz);
                    }
                }
            }
        }

        int mid = 9;
        set(region, ox + mid, oy + 1, oz + mid, Material.CHISELED_NETHER_BRICKS);
        set(region, ox + mid, oy + 2, oz + mid, Material.SOUL_FIRE);

        for (int[] pos : new int[][]{{5,5},{5,13},{13,5},{13,13}}) {
            set(region, ox + pos[0], oy + 1, oz + pos[1], Material.SOUL_SAND);
            set(region, ox + pos[0], oy + 2, oz + pos[1], Material.SOUL_FIRE);
        }

        set(region, ox + 5, oy + 1, oz + mid, Material.CHEST);
    }


    private static void addDecay(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int decayCount = 20 + rng.nextInt(30);
        for (int i = 0; i < decayCount; i++) {
            int dx = rng.nextInt(21);
            int dy = rng.nextInt(10);
            int dz = rng.nextInt(21);
            if (!region.isInRegion(ox + dx, oy + dy, oz + dz)) continue;
            Material m = region.getType(ox + dx, oy + dy, oz + dz);
            if (m == Material.NETHER_BRICKS || m == Material.CHISELED_NETHER_BRICKS) {
                region.setType(ox + dx, oy + dy, oz + dz,
                        rng.nextBoolean() ? Material.CRACKED_NETHER_BRICKS : Material.AIR);
            }
        }
    }


    private static void addLava(LimitedRegion region, int ox, int oy, int oz, Random rng) {

        int[][] spots = {{2, 2}, {2, 16}, {16, 2}, {16, 16}};
        for (int[] s : spots) {
            if (rng.nextBoolean()) continue; // nie każdy narożnik
            int y = oy + 3;
            if (region.isInRegion(ox + s[0], y, oz + s[1])) {
                region.setType(ox + s[0], y, oz + s[1], Material.LAVA);
            }
        }
    }


    private static void set(LimitedRegion r, int x, int y, int z, Material m) {
        if (r.isInRegion(x, y, z)) r.setType(x, y, z, m);
    }

    private static void clear(LimitedRegion r, int x, int y, int z) {
        set(r, x, y, z, Material.AIR);
    }
}