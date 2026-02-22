package com.venomgrave.hexvg.populator.dagobah;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;

public class DagobahPopulator extends com.venomgrave.hexvg.populator.AbstractPopulator {

    private static final Object[][] ORES = {
            { Material.COAL_ORE,  -50,  40, 6, 3 },
            { Material.IRON_ORE,  -60,  20, 5, 2 },
            { Material.CLAY,      -20,  70, 8, 5 },
            { Material.GRAVEL,    -40,  50, 7, 4 },
    };

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo.getMinHeight(), worldInfo.getMaxHeight(), random, baseX, baseZ, region);
        generateVegetation(random, baseX, baseZ, region);
        generateMushrooms(random, baseX, baseZ, region);
        generateLilyPads(random, baseX, baseZ, region);
        generateVines(random, baseX, baseZ, region);
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
            if (m == Material.STONE || m == Material.DIRT || m == Material.GRASS_BLOCK) {
                region.setType(bx, by, bz, mat);
            }
        }
    }


    private static final Material[] SWAMP_PLANTS = {
            Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.DANDELION, Material.POPPY
    };

    private void generateVegetation(Random random, int baseX, int baseZ, LimitedRegion region) {
        int count = 3 + random.nextInt(8);
        for (int i = 0; i < count; i++) {
            int vx = baseX + random.nextInt(16);
            int vz = baseZ + random.nextInt(16);
            int vy = findSolidY(vx, vz, region);
            if (vy < -60 || !region.isInRegion(vx, vy, vz)) continue;
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
        if (my < -60 || !region.isInRegion(mx, my, mz)) return;
        if (region.getType(mx, my, mz) != Material.AIR) return;
        region.setType(mx, my, mz, random.nextBoolean() ? Material.RED_MUSHROOM : Material.BROWN_MUSHROOM);
    }

    private void generateLilyPads(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(3) != 0) return;
        int lx = baseX + random.nextInt(16);
        int lz = baseZ + random.nextInt(16);
        for (int y = 80; y > -64; y--) {
            if (!region.isInRegion(lx, y, lz)) continue;
            Material m = region.getType(lx, y, lz);
            if (m == Material.WATER) {
                if (region.isInRegion(lx, y + 1, lz) && region.getType(lx, y + 1, lz) == Material.AIR) {
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
        if (vy < -60) return;
        int length = 1 + random.nextInt(5);
        for (int dy = 0; dy < length; dy++) {
            int y = vy + 2 + dy;
            if (!region.isInRegion(vx, y, vz)) continue;
            if (region.getType(vx, y, vz) == Material.AIR) {
                region.setType(vx, y, vz, Material.VINE);
            }
        }
    }


    private void generateStructures(HexVGPlanets plugin, Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(500) == 0) placeSchematic(plugin, "dagobah_temple",  random, baseX, baseZ, region);
        if (random.nextInt(200) == 0) placeSchematic(plugin, "dagobah_hut",     random, baseX, baseZ, region);
        if (random.nextInt(300) == 0) placeSchematic(plugin, "dagobah_spiders", random, baseX, baseZ, region);
    }

    private void placeSchematic(HexVGPlanets plugin, String name, Random random,
                                int baseX, int baseZ, LimitedRegion region) {
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
}