package com.venomgrave.hexvg.populator.dagobah;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MushroomHutPopulator extends AbstractPopulator {

    public static final int DEFAULT_RARITY = 250;

    @Override
    public void populate(WorldInfo worldInfo, Random random,
                         int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(DEFAULT_RARITY) != 0) return;

        int bx = chunkX * 16 + 4 + random.nextInt(8);
        int bz = chunkZ * 16 + 4 + random.nextInt(8);
        int by = findSolidY(bx, bz, region) - 1; // fundament poniżej powierzchni

        if (!isSafe(region, bx, by, bz)) return;

        if (region.getType(bx, by, bz) == Material.WATER) return;

        buildHut(region, bx, by, bz, random);
    }


    private static void buildHut(LimitedRegion region, int ox, int oy, int oz, Random rng) {
        int w = 5, h = 4, d = 5;

        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                setBlock(region, ox + dx, oy, oz + dz, Material.MUD_BRICKS);
            }
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dy = 1; dy <= h; dy++) {
                setBlock(region, ox + dx, oy + dy, oz,     wallMat(dx, dy, w, h, rng));
                setBlock(region, ox + dx, oy + dy, oz + d, wallMat(dx, dy, w, h, rng));
            }
        }
        for (int dz = 0; dz < d; dz++) {
            for (int dy = 1; dy <= h; dy++) {
                // Boczne ściany
                setBlock(region, ox,     oy + dy, oz + dz, wallMat(dz, dy, d, h, rng));
                setBlock(region, ox + w, oy + dy, oz + dz, wallMat(dz, dy, d, h, rng));
            }
        }

        int doorX = ox + w / 2;
        setBlock(region, doorX, oy + 1, oz, Material.AIR);
        setBlock(region, doorX, oy + 2, oz, Material.AIR);

        setBlock(region, ox, oy + 2, oz + d / 2, Material.AIR);
        setBlock(region, ox + w, oy + 2, oz + d / 2, Material.AIR);

        buildMushroomRoof(region, ox, oy + h, oz, w, d, rng);

        buildInterior(region, ox, oy, oz, w, h, d);
    }

    private static Material wallMat(int pos, int height, int size, int maxH, Random rng) {
        if (pos == 0 || pos == size - 1) return Material.MUSHROOM_STEM;
        return rng.nextInt(3) == 0 ? Material.BROWN_MUSHROOM_BLOCK : Material.MUSHROOM_STEM;
    }


    private static void buildMushroomRoof(LimitedRegion region,
                                          int ox, int oy, int oz,
                                          int w, int d, Random rng) {
        Material roofMat = rng.nextBoolean()
                ? Material.BROWN_MUSHROOM_BLOCK
                : Material.RED_MUSHROOM_BLOCK;

        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                setBlock(region, ox + dx, oy, oz + dz, roofMat);
            }
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                setBlock(region, ox + dx, oy + 1, oz + dz, roofMat);
            }
        }

        for (int dx = 1; dx < w - 1; dx++) {
            for (int dz = 1; dz < d - 1; dz++) {
                setBlock(region, ox + dx, oy + 2, oz + dz, roofMat);
            }
        }

        setBlock(region, ox + w / 2, oy + 3, oz + d / 2, Material.MUSHROOM_STEM);
    }


    private static void buildInterior(LimitedRegion region,
                                      int ox, int oy, int oz,
                                      int w, int h, int d) {
        // Oczyść środek
        for (int dx = 1; dx < w; dx++) {
            for (int dy = 1; dy < h; dy++) {
                for (int dz = 1; dz < d; dz++) {
                    setBlock(region, ox + dx, oy + dy, oz + dz, Material.AIR);
                }
            }
        }

        setBlock(region, ox + 1, oy + 1, oz + 1, Material.CRAFTING_TABLE);

        setBlock(region, ox + w - 2, oy + 1, oz + 1, Material.CAULDRON);

        setBlock(region, ox + 1, oy + 1, oz + d - 2, Material.BARREL);

        setBlock(region, ox + w / 2, oy + h - 1, oz + d / 2, Material.LANTERN);
    }


    private static void setBlock(LimitedRegion region, int x, int y, int z, Material mat) {
        if (region.isInRegion(x, y, z)) region.setType(x, y, z, mat);
    }
}