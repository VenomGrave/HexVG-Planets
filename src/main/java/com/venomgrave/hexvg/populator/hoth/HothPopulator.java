package com.venomgrave.hexvg.populator.hoth;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;

public class HothPopulator extends com.venomgrave.hexvg.populator.AbstractPopulator {

    private static final Material[] LOG_MATERIALS = {
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG
    };

    private static final Object[][] ORES = {
            { Material.COAL_ORE,     -50,  64,  8, 4 },
            { Material.IRON_ORE,     -60,  30,  6, 3 },
            { Material.GOLD_ORE,     -64,  10,  4, 2 },
            { Material.DIAMOND_ORE,  -64,   2,  3, 1 },
            { Material.LAPIS_ORE,    -64,  20,  5, 2 },
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo.getMinHeight(), worldInfo.getMaxHeight(), random, baseX, baseZ, region);
        generateLogs(random, baseX, baseZ, region);
        generateStructures(plugin, random, baseX, baseZ, region);
    }


    private void generateOres(int worldMinY, int worldMaxY, Random random,
                              int baseX, int baseZ, LimitedRegion region) {
        for (Object[] ore : ORES) {
            Material mat = (Material) ore[0];
            int oreMinY  = Math.max(worldMinY, (int) ore[1]);
            int oreMaxY  = Math.min(worldMaxY - 1, (int) ore[2]);
            int veinSize = (int) ore[3];
            int veins    = (int) ore[4];
            for (int v = 0; v < veins; v++) {
                int ox = baseX + random.nextInt(16);
                int oz = baseZ + random.nextInt(16);
                int oy = oreMinY + random.nextInt(Math.max(1, oreMaxY - oreMinY));
                placeVein(mat, ox, oy, oz, veinSize, random, region);
            }
        }
    }

    private void placeVein(Material mat, int ox, int oy, int oz,
                           int size, Random rng, LimitedRegion region) {
        for (int i = 0; i < size; i++) {
            int bx = ox + rng.nextInt(3) - 1;
            int by = oy + rng.nextInt(3) - 1;
            int bz = oz + rng.nextInt(3) - 1;
            if (!region.isInRegion(bx, by, bz)) continue;
            Material m = region.getType(bx, by, bz);
            if (m == Material.STONE || m == Material.PACKED_ICE || m == Material.ICE) {
                region.setType(bx, by, bz, mat);
            }
        }
    }


    private void generateLogs(Random random, int baseX, int baseZ, LimitedRegion region) {
        int count = random.nextInt(24);
        for (int i = 0; i < count; i++) {
            int lx = baseX + random.nextInt(16);
            int lz = baseZ + random.nextInt(16);
            int surfY = findSurfaceY(lx, lz, region);
            if (surfY < -60) continue;
            if (!region.isInRegion(lx, surfY, lz)) continue;
            Material surf = region.getType(lx, surfY, lz);
            if (surf != Material.ICE && surf != Material.PACKED_ICE && surf != Material.SNOW_BLOCK) continue;

            Material logMat = LOG_MATERIALS[random.nextInt(LOG_MATERIALS.length)];
            int logY   = surfY - 1 - random.nextInt(6);
            int length = 2 + random.nextInt(3);
            int dir    = random.nextInt(3);
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


    private void generateStructures(HexVGPlanets plugin, Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(200) == 0) placeSchematic(plugin, "hoth_base",  random, baseX, baseZ, region);
        if (random.nextInt(350) == 0) placeSchematic(plugin, "hoth_dome",  random, baseX, baseZ, region);
        if (random.nextInt(150) == 0) placeSchematic(plugin, "hoth_room",  random, baseX, baseZ, region);
    }

    private void placeSchematic(HexVGPlanets plugin, String name, Random random, int baseX, int baseZ, LimitedRegion region) {
        Optional<Schematic> opt = plugin.getSchematicRegistry().get(name);
        opt.ifPresent(s -> {
            int sx = baseX + random.nextInt(16);
            int sz = baseZ + random.nextInt(16);
            int sy = findSurfaceY(sx, sz, region);
            if (sy <= -60) return;
            SchematicPlacer.placeInRegion(region, SchematicRotator.rotate(s, random.nextInt(4)),
                    sx, sy, sz, plugin.getLootGenerator());
        });
    }
}