package com.venomgrave.hexvg.populator.mustafar;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;
import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MustafarPopulator extends AbstractPopulator {

    // mat, minY, maxY, veinSize, veinsPerChunk
    private static final Object[][] ORES = {
            { Material.IRON_ORE,          -60,  80,  5, 3 },
            { Material.GOLD_ORE,          -64,  40,  4, 3 },
            { Material.ANCIENT_DEBRIS,    -64,  16,  2, 1 },
            { Material.NETHER_QUARTZ_ORE, -20, 120,  6, 4 },
            { Material.NETHER_GOLD_ORE,   -30,  80,  5, 3 },
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        generateOres(worldInfo, random, chunkX, chunkZ, region);
        generateObsidianSpires(random, chunkX, chunkZ, region);
        generateLavaBubbles(random, chunkX, chunkZ, region);
        generateBasaltClusters(random, chunkX, chunkZ, region);
        generateStructures(plugin, worldInfo, random, chunkX, chunkZ, region);
    }

    private void generateOres(WorldInfo worldInfo, Random random,
                              int chunkX, int chunkZ, LimitedRegion region) {

        int worldMinY = worldInfo.getMinHeight();
        int worldMaxY = worldInfo.getMaxHeight();
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (Object[] ore : ORES) {
            Material mat = (Material) ore[0];
            int oreMinY  = Math.max(worldMinY, (int) ore[1]);
            int oreMaxY  = Math.min(worldMaxY - 1, (int) ore[2]);
            int veinSize = (int) ore[3];
            int veins    = (int) ore[4];

            if (oreMaxY <= oreMinY) continue;

            for (int v = 0; v < veins; v++) {
                int ox = baseX + random.nextInt(16);
                int oz = baseZ + random.nextInt(16);
                int oy = oreMinY + random.nextInt(Math.max(1, oreMaxY - oreMinY));

                placeOreVein(region, mat, ox, oy, oz, veinSize, random,
                        Material.STONE, Material.BASALT, Material.BLACKSTONE);
            }
        }
    }

    private void generateObsidianSpires(Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(10) != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = findLavaTopY(x, z, region);

        if (y < WORLD_MIN_Y) return;

        int height = 2 + random.nextInt(8);
        for (int dy = 0; dy <= height; dy++) {
            int fy = y + dy;
            if (!region.isInRegion(x, fy, z)) continue;
            Material m = region.getType(x, fy, z);
            if (m == Material.AIR || m == Material.LAVA) {
                region.setType(x, fy, z, Material.OBSIDIAN);
            }
        }
    }

    private void generateLavaBubbles(Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(6) != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int surface = findLavaTopY(x, z, region);

        int y = surface - 3 - random.nextInt(10);
        if (y < WORLD_MIN_Y) return;

        int radius = 1 + random.nextInt(2);

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int fx = x + dx, fy = y + dy, fz = z + dz;
                    if (!region.isInRegion(fx, fy, fz)) continue;

                    Material m = region.getType(fx, fy, fz);
                    if (m == Material.STONE || m == Material.BASALT) {
                        region.setType(fx, fy, fz, Material.AIR);
                    }
                }
            }
        }
    }

    private void generateBasaltClusters(Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(4) != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = findSolidY(x, z, region);

        if (y < WORLD_MIN_Y) return;

        int count = 3 + random.nextInt(8);

        for (int i = 0; i < count; i++) {
            int bx = x + random.nextInt(5) - 2;
            int by = y - random.nextInt(4);
            int bz = z + random.nextInt(5) - 2;

            if (!region.isInRegion(bx, by, bz)) continue;

            if (region.getType(bx, by, bz) == Material.STONE) {
                region.setType(bx, by, bz, Material.BASALT);
            }
        }
    }

    private void generateStructures(HexVGPlanets plugin, WorldInfo worldInfo,
                                    Random random, int chunkX, int chunkZ, LimitedRegion region) {

        String wn = worldInfo.getName();

        int rarityBase   = getStructureRarity(wn, "mustafarbase", 400);
        int rarityTemple = getStructureRarity(wn, "mustafartemple", 500);
        int rarityFount  = getStructureRarity(wn, "bases", 150);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "mustafar_base", rarityBase, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "mustafar_temple", rarityTemple, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "mustafar_lava_fountain", rarityFount, random);
    }
}
