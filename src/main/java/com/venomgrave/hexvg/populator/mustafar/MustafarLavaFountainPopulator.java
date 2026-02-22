package com.venomgrave.hexvg.populator.mustafar;

import com.venomgrave.hexvg.populator.AbstractPopulator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MustafarLavaFountainPopulator extends AbstractPopulator {

    public static final int DEFAULT_RARITY = 50;

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(DEFAULT_RARITY) != 0) return;

        int bx = chunkX * 16 + 2 + random.nextInt(12);
        int bz = chunkZ * 16 + 2 + random.nextInt(12);
        int by = findSolidY(bx, bz, region) - 1;

        if (!isSafe(region, bx, by, bz)) return;

        switch (random.nextInt(3)) {
            case 0: buildGeyser(region, bx, by, bz, random);   break;
            case 1: buildCascade(region, bx, by, bz, random);  break;
            case 2: buildCrater(region, bx, by, bz, random);   break;
        }
    }


    private static void buildGeyser(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int height = 5 + rng.nextInt(8);

        for (int dy = -2; dy <= height; dy++) {
            Material mat = (dy < 0) ? Material.MAGMA_BLOCK
                    : (rng.nextInt(4) == 0) ? Material.BASALT
                    : Material.MAGMA_BLOCK;
            set(region, ox, oy + dy, oz, mat);
            if (dy < 3) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        set(region, ox + dx, oy + dy, oz + dz, Material.BASALT);
                    }
                }
            }
        }

        set(region, ox, oy + height + 1, oz, Material.LAVA);

        int spreadR = 2 + rng.nextInt(3);
        for (int dx = -spreadR; dx <= spreadR; dx++) {
            for (int dz = -spreadR; dz <= spreadR; dz++) {
                if (dx * dx + dz * dz > spreadR * spreadR) continue;
                if (!region.isInRegion(ox + dx, oy - 1, oz + dz)) continue;
                if (region.getType(ox + dx, oy - 1, oz + dz) == Material.AIR) {
                    region.setType(ox + dx, oy - 1, oz + dz, Material.LAVA);
                }
            }
        }

        for (int dx = -spreadR - 1; dx <= spreadR + 1; dx++) {
            for (int dz = -spreadR - 1; dz <= spreadR + 1; dz++) {
                int dist = dx * dx + dz * dz;
                if (dist <= spreadR * spreadR + 2 && dist > spreadR * spreadR - 2) {
                    set(region, ox + dx, oy - 1, oz + dz, Material.MAGMA_BLOCK);
                }
            }
        }
    }


    private static void buildCascade(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int height = 4 + rng.nextInt(5);

        for (int dy = 0; dy <= height; dy++) {
            int radius = height - dy; // im wyżej, tym węższy
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) continue;
                    Material mat = (dy == height) ? Material.MAGMA_BLOCK
                            : rng.nextInt(3) == 0 ? Material.BLACKSTONE
                            : Material.BASALT;
                    set(region, ox + dx, oy + dy, oz + dz, mat);
                }
            }
        }

        set(region, ox, oy + height + 1, oz, Material.LAVA);

        for (int[] side : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            if (rng.nextBoolean()) continue;
            int sx = ox + side[0] * (height / 2);
            int sz = oz + side[1] * (height / 2);
            int sy = oy + height / 2;
            set(region, sx, sy, sz, Material.LAVA);
        }
    }


    private static void buildCrater(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int outerR = 4 + rng.nextInt(4);
        int innerR = outerR - 2;
        int rimH   = 2 + rng.nextInt(2);

        for (int dx = -outerR; dx <= outerR; dx++) {
            for (int dz = -outerR; dz <= outerR; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > outerR * outerR) continue;

                if (d2 <= innerR * innerR) {
                    for (int dy = -1; dy <= 0; dy++) {
                        set(region, ox + dx, oy + dy, oz + dz, Material.LAVA);
                    }
                } else {
                    for (int dy = 0; dy <= rimH; dy++) {
                        Material mat = (dy == rimH) ? Material.MAGMA_BLOCK
                                : rng.nextInt(4) == 0 ? Material.BLACKSTONE
                                : Material.BASALT;
                        set(region, ox + dx, oy + dy, oz + dz, mat);
                    }
                }
            }
        }
    }


    private static void set(LimitedRegion region, int x, int y, int z, Material mat) {
        if (region.isInRegion(x, y, z)) region.setType(x, y, z, mat);
    }
}