package com.venomgrave.hexvg.populator.mustafar;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;

public class MustafarPopulator extends com.venomgrave.hexvg.populator.AbstractPopulator {

    private static final Object[][] ORES = {
            { Material.IRON_ORE,          -60,  80,  5, 3 },
            { Material.GOLD_ORE,          -64,  40,  4, 3 },
            { Material.ANCIENT_DEBRIS,    -64,  16,  2, 1 },
            { Material.NETHER_QUARTZ_ORE,  -20, 120,  6, 4 },
            { Material.NETHER_GOLD_ORE,    -30,  80,  5, 3 },
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo.getMinHeight(), worldInfo.getMaxHeight(), random, baseX, baseZ, region);
        generateObsidianSpires(random, baseX, baseZ, region);
        generateLavaBubbles(random, baseX, baseZ, region);
        generateBasaltClusters(random, baseX, baseZ, region);
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
            if (m == Material.STONE || m == Material.BASALT || m == Material.BLACKSTONE) {
                region.setType(bx, by, bz, mat);
            }
        }
    }


    private void generateObsidianSpires(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(10) != 0) return;

        int sx = baseX + random.nextInt(16);
        int sz = baseZ + random.nextInt(16);
        int sy = findLavaSurfaceY(sx, sz, region);
        if (sy < -60) return;

        int height = 2 + random.nextInt(8);
        for (int dy = 0; dy <= height; dy++) {
            if (!region.isInRegion(sx, sy + dy, sz)) continue;
            Material m = region.getType(sx, sy + dy, sz);
            if (m == Material.AIR || m == Material.LAVA) {
                region.setType(sx, sy + dy, sz, Material.OBSIDIAN);
            }
        }
    }


    private void generateLavaBubbles(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(6) != 0) return;

        int bx = baseX + random.nextInt(16);
        int bz = baseZ + random.nextInt(16);
        int by = findLavaSurfaceY(bx, bz, region) - 3 - random.nextInt(10);

        if (by < -60) return;
        int radius = 1 + random.nextInt(2);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int fx = bx + dx, fy = by + dy, fz = bz + dz;
                    if (!region.isInRegion(fx, fy, fz)) continue;
                    Material m = region.getType(fx, fy, fz);
                    if (m == Material.STONE || m == Material.BASALT) {
                        region.setType(fx, fy, fz, Material.AIR);
                    }
                }
            }
        }
    }


    private void generateBasaltClusters(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(4) != 0) return;

        int cx = baseX + random.nextInt(16);
        int cz = baseZ + random.nextInt(16);
        int cy = findSolidY(cx, cz, region);
        if (cy < -60) return;

        int count = 3 + random.nextInt(8);
        for (int i = 0; i < count; i++) {
            int bx = cx + random.nextInt(5) - 2;
            int by = cy - random.nextInt(4);
            int bz = cz + random.nextInt(5) - 2;
            if (!region.isInRegion(bx, by, bz)) continue;
            Material m = region.getType(bx, by, bz);
            if (m == Material.STONE) {
                region.setType(bx, by, bz, Material.BASALT);
            }
        }
    }


    private void generateStructures(HexVGPlanets plugin, Random random, int baseX, int baseZ, LimitedRegion region) {

        if (random.nextInt(400) == 0) placeSchematic(plugin, "mustafar_base",   random, baseX, baseZ, region);

        if (random.nextInt(500) == 0) placeSchematic(plugin, "mustafar_temple", random, baseX, baseZ, region);

        if (random.nextInt(150) == 0) placeSchematic(plugin, "mustafar_lava_fountain", random, baseX, baseZ, region);
    }

    private void placeSchematic(HexVGPlanets plugin, String name, Random random, int baseX, int baseZ, LimitedRegion region) {
        Optional<Schematic> opt = plugin.getSchematicRegistry().get(name);
        opt.ifPresent(s -> {
            int sx = baseX + random.nextInt(16);
            int sz = baseZ + random.nextInt(16);
            int sy = findSolidY(sx, sz, region);
            if (sy <= -60) return;
            SchematicPlacer.placeInRegion(region, SchematicRotator.rotate(s, random.nextInt(4)),
                    sx, sy, sz, plugin.getLootGenerator());
        });
    }


    private int findLavaSurfaceY(int x, int z, LimitedRegion region) {
        for (int y = 150; y > -64; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            if (region.getType(x, y, z) == Material.LAVA) return y + 1;
        }
        return -64;
    }

}