package com.venomgrave.hexvg.populator.dagobah;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class SpiderForestPopulator extends AbstractPopulator {

    public static final int DEFAULT_RARITY = 120;

    @Override
    public void populate(WorldInfo worldInfo, Random random,
                         int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(DEFAULT_RARITY) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        placeWebs(random, baseX, baseZ, region);
        placeStumps(random, baseX, baseZ, region);
        placeBones(random, baseX, baseZ, region);
        placeCocoon(random, baseX, baseZ, region);
    }


    private static void placeWebs(Random rng, int baseX, int baseZ, LimitedRegion region) {
        int webCount = 20 + rng.nextInt(30);
        for (int i = 0; i < webCount; i++) {
            int wx = baseX + rng.nextInt(16);
            int wz = baseZ + rng.nextInt(16);
            int wy = findSolidY(wx, wz, region);
            if (wy < WORLD_MIN_Y + 2) continue;

            int spread = 1 + rng.nextInt(3);
            for (int dx = -spread; dx <= spread; dx++) {
                for (int dy = 0; dy <= spread; dy++) {
                    for (int dz = -spread; dz <= spread; dz++) {
                        if (dx * dx + dy * dy + dz * dz > spread * spread) continue;
                        int tx = wx + dx, ty = wy + dy, tz = wz + dz;
                        if (!region.isInRegion(tx, ty, tz)) continue;
                        Material m = region.getType(tx, ty, tz);
                        if (m.isAir() || m == Material.JUNGLE_LEAVES
                                || m == Material.OAK_LEAVES) {
                            if (rng.nextInt(3) != 0) {
                                region.setType(tx, ty, tz, Material.COBWEB);
                            }
                        }
                    }
                }
            }
        }
    }


    private static void placeStumps(Random rng, int baseX, int baseZ, LimitedRegion region) {
        int stumpCount = 2 + rng.nextInt(5);
        for (int i = 0; i < stumpCount; i++) {
            int sx = baseX + rng.nextInt(16);
            int sz = baseZ + rng.nextInt(16);
            int sy = findSolidY(sx, sz, region) - 1;
            if (sy < WORLD_MIN_Y + 2) continue;
            if (!region.isInRegion(sx, sy, sz)) continue;
            if (region.getType(sx, sy, sz) == Material.WATER) continue;

            int stumpH = 2 + rng.nextInt(3);
            for (int dy = 0; dy <= stumpH; dy++) {
                if (!region.isInRegion(sx, sy + dy, sz)) continue;
                region.setType(sx, sy + dy, sz, Material.JUNGLE_LOG);
            }

            if (region.isInRegion(sx, sy + stumpH + 1, sz)) {
                region.setType(sx, sy + stumpH + 1, sz, Material.MOSS_BLOCK);
            }

            int webSide = rng.nextInt(4);
            int[] offX = {1, -1, 0,  0};
            int[] offZ = {0,  0, 1, -1};
            for (int dy = 1; dy <= stumpH; dy++) {
                int tx = sx + offX[webSide];
                int tz = sz + offZ[webSide];
                if (!region.isInRegion(tx, sy + dy, tz)) continue;
                if (region.getType(tx, sy + dy, tz).isAir()) {
                    region.setType(tx, sy + dy, tz, Material.COBWEB);
                }
            }
        }
    }


    private static void placeBones(Random rng, int baseX, int baseZ, LimitedRegion region) {
        int boneCount = 1 + rng.nextInt(4);
        for (int i = 0; i < boneCount; i++) {
            int bx = baseX + rng.nextInt(16);
            int bz = baseZ + rng.nextInt(16);
            int by = findSolidY(bx, bz, region);
            if (by < WORLD_MIN_Y + 1) continue;
            if (!region.isInRegion(bx, by, bz)) continue;
            if (region.getType(bx, by, bz) != Material.AIR) continue;
            region.setType(bx, by, bz, Material.BONE_BLOCK);
            if (region.isInRegion(bx, by + 1, bz)) {
                region.setType(bx, by + 1, bz, Material.COBWEB);
            }
        }
    }


    private static void placeCocoon(Random rng, int baseX, int baseZ, LimitedRegion region) {
        if (rng.nextInt(3) != 0) return;

        int cx = baseX + 3 + rng.nextInt(10);
        int cz = baseZ + 3 + rng.nextInt(10);
        int cy = findSolidY(cx, cz, region);
        if (cy < WORLD_MIN_Y + 2) return;
        cy++;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int tx = cx + dx, ty = cy + dy, tz = cz + dz;
                    if (!region.isInRegion(tx, ty, tz)) continue;

                    boolean wall = dx == -1 || dx == 1 || dz == -1 || dz == 1
                            || dy == 0 || dy == 2;
                    if (wall) {
                        region.setType(tx, ty, tz, Material.COBWEB);
                    } else {

                        if (dy == 1) region.setType(tx, ty, tz, Material.BONE_BLOCK);
                    }
                }
            }
        }
    }
}