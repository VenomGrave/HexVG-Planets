package com.venomgrave.hexvg.populator.hoth;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.AbstractPopulator;
import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class HothPopulator extends AbstractPopulator {

    private static final Material[] LOG_MATERIALS = {
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG
    };

    // mat, minY, maxY, veinSize, veinsPerChunk
    private static final Object[][] ORES = {
            { Material.COAL_ORE,    -50,  64,  8, 4 },
            { Material.IRON_ORE,    -60,  30,  6, 3 },
            { Material.GOLD_ORE,    -64,  10,  4, 2 },
            { Material.DIAMOND_ORE, -64,   2,  3, 1 },
            { Material.LAPIS_ORE,   -64,  20,  5, 2 },
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo, random, chunkX, chunkZ, region);
        generateLogs(random, baseX, baseZ, region);
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
                        Material.STONE, Material.PACKED_ICE, Material.ICE);
            }
        }
    }

    private void generateLogs(Random random, int baseX, int baseZ, LimitedRegion region) {
        int count = random.nextInt(24);
        for (int i = 0; i < count; i++) {
            int lx = baseX + random.nextInt(16);
            int lz = baseZ + random.nextInt(16);
            int surfY = findSurfaceY(lx, lz, region);
            if (surfY < WORLD_MIN_Y) continue;
            if (!region.isInRegion(lx, surfY, lz)) continue;

            Material surf = region.getType(lx, surfY, lz);
            if (surf != Material.ICE && surf != Material.PACKED_ICE && surf != Material.SNOW_BLOCK) continue;

            Material logMat = LOG_MATERIALS[random.nextInt(LOG_MATERIALS.length)];
            int logY   = surfY - 1 - random.nextInt(6);
            int length = 2 + random.nextInt(3);
            int dir    = random.nextInt(3); // 0=vertical,1=x,2=z

            for (int j = 0; j < length; j++) {
                int bx = lx + (dir == 1 ? j : 0);
                int by = logY + (dir == 0 ? j : 0);
                int bz = lz + (dir == 2 ? j : 0);
                if (!region.isInRegion(bx, by, bz)) continue;
                Material m = region.getType(bx, by, bz);
                if (m == Material.ICE || m == Material.PACKED_ICE || m == Material.SNOW_BLOCK) {
                    region.setType(bx, by, bz, logMat);
                }
            }
        }
    }

    private void generateStructures(HexVGPlanets plugin, WorldInfo worldInfo,
                                    Random random, int chunkX, int chunkZ, LimitedRegion region) {
        String wn = worldInfo.getName();

        int rarityBase = getStructureRarity(wn, "bases", 200);
        int rarityDome = getStructureRarity(wn, "domes", 350);
        int rarityRoom = getStructureRarity(wn, "bases", 150);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "hoth_base", rarityBase, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "hoth_dome", rarityDome, random);

        tryPlaceSchematic(region, worldInfo, chunkX, chunkZ,
                "hoth_room", rarityRoom, random);
    }
}
