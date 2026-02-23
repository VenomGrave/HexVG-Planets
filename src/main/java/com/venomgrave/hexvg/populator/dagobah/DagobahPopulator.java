package com.venomgrave.hexvg.populator.dagobah;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;
import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class DagobahPopulator extends AbstractPopulator {

    // mat, minY, maxY, veinSize, veinsPerChunk
    private static final Object[][] ORES = {
            { Material.COAL_ORE,  -50,  40, 6, 3 },
            { Material.IRON_ORE,  -60,  20, 5, 2 },
            { Material.CLAY,      -20,  70, 8, 5 },
            { Material.GRAVEL,    -40,  50, 7, 4 },
    };

    private static final Material[] SWAMP_PLANTS = {
            Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.DANDELION, Material.POPPY
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo, random, chunkX, chunkZ, region);
        generateVegetation(random, baseX, baseZ, region);
        generateMushrooms(random, baseX, baseZ, region);
        generateLilyPads(random, baseX, baseZ, region);
        generateVines(random, baseX, baseZ, region);
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
                        Material.STONE, Material.DIRT, Material.GRASS_BLOCK);
            }
        }
    }

    private void generateVegetation(Random random, int baseX, int baseZ, LimitedRegion region) {
        int count = 3 + random.nextInt(8);
        for (int i = 0; i < count; i++) {
            int vx = baseX + random.nextInt(16);
            int vz = baseZ + random.nextInt(16);
            int vy = findSolidY(vx, vz, region);
            if (vy < WORLD_MIN_Y || !region.isInRegion(vx, vy, vz)) continue;
            if (region.getType(vx, vy, vz) == Material.AIR) {
                region.setType(vx, vy, vz, SWAMP_PLANTS[random.nextInt(SWAMP_PLANTS.length)]);
            }
        }
    }

    private void generateMushrooms(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(4) != 0) return;
        int mx = baseX + random.nextInt(16);
        int mz = baseZ + random.nextInt(16);
        int my = findSolidY(mx, mz, region);
        if (my < WORLD_MIN_Y || !region.isInRegion(mx, my, mz)) return;
        if (region.getType(mx, my, mz) != Material.AIR) return;
        region.setType(mx, my, mz,
                random.nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
    }

    private void generateLilyPads(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(3) != 0) return;
        int lx = baseX + random.nextInt(16);
        int lz = baseZ + random.nextInt(16);
        for (int y = SCAN_TOP_Y; y > WORLD_MIN_Y; y--) {
            if (!region.isInRegion(lx, y, lz)) continue;
            Material m = region.getType(lx, y, lz);
            if (m == Material.WATER) {
                if (region.isInRegion(lx, y + 1, lz)
                        && region.getType(lx, y + 1, lz) == Material.AIR) {
                    region.setType(lx, y + 1, lz, Material.LILY_PAD);
                }
                return;
            } else if (m != Material.AIR) {
                return;
            }
        }
    }

    private void generateVines(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(5) != 0) return;
        int vx = baseX + random.nextInt(16);
        int vz = baseZ + random.nextInt(16);
        int vy = findSolidY(vx, vz, region);
        if (vy < WORLD_MIN_Y) return;
        int length = 1 + random.nextInt(5);
        for (int dy = 0; dy < length; dy++) {
            int y = vy + 2 + dy;
            if (!region.isInRegion(vx, y, vz)) continue;
            if (region.getType(vx, y, vz) == Material.AIR) {
                region.setType(vx, y, vz, Material.VINE);
            }
        }
    }

    private void generateStructures(HexVGPlanets plugin, WorldInfo worldInfo,
                                    Random random, int chunkX, int chunkZ, LimitedRegion region) {
        String wn = worldInfo.getName();

        int rarityTemple  = getStructureRarity(wn, "swamptemple", 500);
        int rarityHut     = getStructureRarity(wn, "treehut",     200);
        int raritySpiders = getStructureRarity(wn, "spiderforest",300);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "dagobah_temple", rarityTemple, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "dagobah_hut", rarityHut, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "dagobah_spiders", raritySpiders, random);
    }
}
