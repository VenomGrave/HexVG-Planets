package com.venomgrave.hexvg.populator.tatooine;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;
import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class TatooinePopulator extends AbstractPopulator {

    // mat, minY, maxY, veinSize, veinsPerChunk
    private static final Object[][] ORES = {
            { Material.COAL_ORE,     -40,  50,  6, 3 },
            { Material.IRON_ORE,     -60,  20,  5, 2 },
            { Material.GOLD_ORE,     -64,   8,  4, 2 },
            { Material.REDSTONE_ORE, -64,  16,  5, 2 },
            { Material.QUARTZ_BLOCK, -30,  60,  6, 3 }, // kryształy kwarcowe
    };

    private static final int CACTUS_CHANCE = 3;

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        generateOres(worldInfo, random, chunkX, chunkZ, region);
        generateCacti(random, chunkX, chunkZ, region);
        generateBones(random, chunkX, chunkZ, region);
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
                        Material.STONE, Material.SANDSTONE, Material.SAND);
            }
        }
    }

    private void generateCacti(Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(CACTUS_CHANCE) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        int count = 1 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            int cx = baseX + random.nextInt(16);
            int cz = baseZ + random.nextInt(16);
            int cy = findSandSurfaceY(cx, cz, region);
            if (cy < WORLD_MIN_Y) continue;

            int height = 1 + random.nextInt(3);
            for (int dy = 0; dy < height; dy++) {
                if (!region.isInRegion(cx, cy + dy, cz)) continue;
                if (region.getType(cx, cy + dy, cz) == Material.AIR) {
                    region.setType(cx, cy + dy, cz, Material.CACTUS);
                } else break;
            }
        }
    }

    private void generateBones(Random random, int chunkX, int chunkZ, LimitedRegion region) {
        if (random.nextInt(20) != 0) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        int bx = baseX + random.nextInt(16);
        int bz = baseZ + random.nextInt(16);
        int by = findSandSurfaceY(bx, bz, region);
        if (by < WORLD_MIN_Y) return;

        int count = 2 + random.nextInt(5);
        for (int i = 0; i < count; i++) {
            int fx = bx + random.nextInt(5) - 2;
            int fz = bz + random.nextInt(5) - 2;
            int fy = by - random.nextInt(3);

            if (!region.isInRegion(fx, fy, fz)) continue;

            Material m = region.getType(fx, fy, fz);
            if (m == Material.SAND || m == Material.SANDSTONE) {
                region.setType(fx, fy, fz, Material.BONE_BLOCK);
            }
        }
    }

    private void generateStructures(HexVGPlanets plugin, WorldInfo worldInfo,
                                    Random random, int chunkX, int chunkZ, LimitedRegion region) {

        String wn = worldInfo.getName();

        int raritySarlacc = getStructureRarity(wn, "sarlacc", 400);
        int rarityHut     = getStructureRarity(wn, "village", 180);
        int rarityBase    = getStructureRarity(wn, "bases", 300);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "tatooine_sarlacc", raritySarlacc, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "tatooine_hut", rarityHut, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "tatooine_base", rarityBase, random);
    }
}
