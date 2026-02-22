package com.venomgrave.hexvg.populator.dagobah;

import com.venomgrave.hexvg.populator.AbstractPopulator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class SwampTemplePopulator extends AbstractPopulator {

    public static final int DEFAULT_RARITY = 450;

    private static final Material[] WALL_MATS = {
            Material.STONE_BRICKS,
            Material.MOSSY_STONE_BRICKS,
            Material.CRACKED_STONE_BRICKS,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
    };

    private static final Material[] FLOOR_MATS = {
            Material.MUD_BRICKS,
            Material.MOSSY_COBBLESTONE,
            Material.STONE_BRICKS,
            Material.GRAVEL,
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random,
                         int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(DEFAULT_RARITY) != 0) return;

        int bx = chunkX * 16 + 3 + random.nextInt(10);
        int bz = chunkZ * 16 + 3 + random.nextInt(10);
        int by = findSolidY(bx, bz, region) - 1;

        if (!isSafe(region, bx, by, bz)) return;
        if (region.getType(bx, by, bz) == Material.WATER) return;

        buildTemple(region, bx, by, bz, random);
    }


    private static void buildTemple(LimitedRegion region, int ox, int oy, int oz, Random rng) {

        int w = 11, h = 7;

        buildFoundation(region, ox, oy, oz, w, rng);
        buildWalls(region, ox, oy, oz, w, h, rng);
        buildColumns(region, ox, oy, oz, w, h, rng);
        buildRoof(region, ox, oy, oz, w, h, rng);
        buildInterior(region, ox, oy, oz, w, h, rng);
        buildStairs(region, ox, oy, oz, w, rng);
        addDecay(region, ox, oy, oz, w, h, rng);
        addVegetation(region, ox, oy, oz, w, h, rng);
    }


    private static void buildFoundation(LimitedRegion region, int ox, int oy, int oz,
                                        int w, Random rng) {
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= w; dz++) {
                if (!region.isInRegion(ox + dx, oy, oz + dz)) continue;
                region.setType(ox + dx, oy, oz + dz,
                        FLOOR_MATS[rng.nextInt(FLOOR_MATS.length)]);

                if (region.isInRegion(ox + dx, oy - 1, oz + dz)) {
                    region.setType(ox + dx, oy - 1, oz + dz, Material.MUD_BRICKS);
                }
            }
        }
    }


    private static void buildWalls(LimitedRegion region, int ox, int oy, int oz,
                                   int w, int h, Random rng) {
        for (int dy = 1; dy <= h; dy++) {
            for (int i = 0; i < w; i++) {

                setWall(region, ox + i, oy + dy, oz,     rng);
                setWall(region, ox + i, oy + dy, oz + w, rng);

                setWall(region, ox,     oy + dy, oz + i, rng);
                setWall(region, ox + w, oy + dy, oz + i, rng);
            }
        }


        int door = ox + w / 2;
        for (int dy = 1; dy <= 3; dy++) {
            clearIfSafe(region, door,     oy + dy, oz);
            clearIfSafe(region, door - 1, oy + dy, oz);
        }
    }

    private static void setWall(LimitedRegion region, int x, int y, int z, Random rng) {
        if (!region.isInRegion(x, y, z)) return;
        region.setType(x, y, z, WALL_MATS[rng.nextInt(WALL_MATS.length)]);
    }


    private static void buildColumns(LimitedRegion region, int ox, int oy, int oz,
                                     int w, int h, Random rng) {
        int[][] corners = { {1, 1}, {1, w - 1}, {w - 1, 1}, {w - 1, w - 1},
                {1, w / 2}, {w - 1, w / 2} };
        for (int[] c : corners) {
            for (int dy = 1; dy <= h + 1; dy++) {
                if (!region.isInRegion(ox + c[0], oy + dy, oz + c[1])) continue;
                region.setType(ox + c[0], oy + dy, oz + c[1],
                        rng.nextInt(6) == 0 ? Material.CRACKED_STONE_BRICKS
                                : Material.STONE_BRICKS);
            }
        }
    }


    private static void buildRoof(LimitedRegion region, int ox, int oy, int oz,
                                  int w, int h, Random rng) {
        int roofY = oy + h + 1;
        // Płaski dach z MUD_BRICKS
        for (int dx = 0; dx <= w; dx++) {
            for (int dz = 0; dz <= w; dz++) {
                if (!region.isInRegion(ox + dx, roofY, oz + dz)) continue;
                region.setType(ox + dx, roofY, oz + dz,
                        rng.nextInt(5) == 0 ? Material.MOSSY_STONE_BRICKS : Material.MUD_BRICKS);
            }
        }

        for (int dx = 2; dx <= w - 2; dx++) {
            for (int dz = 2; dz <= w - 2; dz++) {
                if (!region.isInRegion(ox + dx, roofY + 1, oz + dz)) continue;
                region.setType(ox + dx, roofY + 1, oz + dz, Material.MUD_BRICKS);
            }
        }
        if (region.isInRegion(ox + w / 2, roofY + 2, oz + w / 2)) {
            region.setType(ox + w / 2, roofY + 2, oz + w / 2, Material.CHISELED_STONE_BRICKS);
        }
    }


    private static void buildInterior(LimitedRegion region, int ox, int oy, int oz,
                                      int w, int h, Random rng) {

        for (int dx = 1; dx < w; dx++) {
            for (int dy = 1; dy <= h; dy++) {
                for (int dz = 1; dz < w; dz++) {
                    clearIfSafe(region, ox + dx, oy + dy, oz + dz);
                }
            }
        }

        int mid = w / 2;

        for (int dx = mid - 1; dx <= mid + 1; dx++) {
            for (int dz = mid - 1; dz <= mid + 1; dz++) {
                setIfSafe(region, ox + dx, oy + 1, oz + dz, Material.CHISELED_STONE_BRICKS);
            }
        }
        setIfSafe(region, ox + mid, oy + 2, oz + mid, Material.ENCHANTING_TABLE);

        setIfSafe(region, ox + 2, oy + 1, oz + 2, Material.CHEST);

        for (int[] pos : new int[][]{{1, 1}, {1, w-1}, {w-1, 1}, {w-1, w-1}}) {
            setIfSafe(region, ox + pos[0], oy + 2, oz + pos[1], Material.LANTERN);
        }

        if (rng.nextBoolean()) {
            setIfSafe(region, ox + 2, oy + 1, oz + w - 2, Material.SPAWNER);
        }
    }


    private static void buildStairs(LimitedRegion region, int ox, int oy, int oz, int w, Random rng) {
        int doorX = ox + w / 2;
        for (int step = 1; step <= 4; step++) {
            int sy = oy - step;
            for (int dx = -1; dx <= 1; dx++) {
                if (!region.isInRegion(doorX + dx, sy, oz - step)) continue;
                region.setType(doorX + dx, sy, oz - step,
                        rng.nextInt(3) == 0 ? Material.MOSSY_COBBLESTONE : Material.MUD_BRICKS);
            }
        }
    }


    private static void addDecay(LimitedRegion region, int ox, int oy, int oz, int w, int h, Random rng) {
        int decayCount = 15 + rng.nextInt(25);
        for (int i = 0; i < decayCount; i++) {
            int dx = rng.nextInt(w + 2) - 1;
            int dy = 1 + rng.nextInt(h);
            int dz = rng.nextInt(w + 2) - 1;
            if (!region.isInRegion(ox + dx, oy + dy, oz + dz)) continue;
            Material m = region.getType(ox + dx, oy + dy, oz + dz);
            if (m == Material.STONE_BRICKS || m == Material.MOSSY_STONE_BRICKS
                    || m == Material.MUD_BRICKS) {
                region.setType(ox + dx, oy + dy, oz + dz,
                        rng.nextBoolean() ? Material.AIR : Material.CRACKED_STONE_BRICKS);
            }
        }
    }


    private static void addVegetation(LimitedRegion region, int ox, int oy, int oz, int w, int h, Random rng) {
        int vineCount = 10 + rng.nextInt(20);
        for (int i = 0; i < vineCount; i++) {
            int vx = ox + rng.nextInt(w + 2) - 1;
            int vz = oz + rng.nextInt(w + 2) - 1;
            int startY = oy + h + 1;
            int length = 2 + rng.nextInt(h - 1);
            for (int dy = 0; dy < length; dy++) {
                if (!region.isInRegion(vx, startY - dy, vz)) continue;
                if (region.getType(vx, startY - dy, vz).isAir()) {
                    region.setType(vx, startY - dy, vz, Material.VINE);
                } else break;
            }
        }

        int mossCount = 8 + rng.nextInt(12);
        for (int i = 0; i < mossCount; i++) {
            int mx = ox + rng.nextInt(w + 4) - 2;
            int mz = oz + rng.nextInt(w + 4) - 2;
            int my = oy + h + 2;
            if (!region.isInRegion(mx, my, mz)) continue;
            if (region.getType(mx, my, mz).isAir()) {
                region.setType(mx, my, mz, Material.MOSS_BLOCK);
            }
        }
    }


    private static void setIfSafe(LimitedRegion region, int x, int y, int z, Material mat) {
        if (region.isInRegion(x, y, z)) region.setType(x, y, z, mat);
    }

    private static void clearIfSafe(LimitedRegion region, int x, int y, int z) {
        if (region.isInRegion(x, y, z)) region.setType(x, y, z, Material.AIR);
    }
}