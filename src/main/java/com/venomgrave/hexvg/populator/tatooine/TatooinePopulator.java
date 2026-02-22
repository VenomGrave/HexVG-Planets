package com.venomgrave.hexvg.populator.tatooine;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;


public class TatooinePopulator extends com.venomgrave.hexvg.populator.AbstractPopulator {

    private static final Object[][] ORES = {
            { Material.COAL_ORE,       -40,  50,  6, 3 },
            { Material.IRON_ORE,       -60,  20,  5, 2 },
            { Material.GOLD_ORE,       -64,   8,  4, 2 },
            { Material.REDSTONE_ORE,   -64,  16,  5, 2 },
            { Material.QUARTZ_BLOCK,   -30,  60,  6, 3 }, // kryształy kwarcowe
    };

    private static final int CACTUS_CHANCE = 3;

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        generateOres(worldInfo.getMinHeight(), worldInfo.getMaxHeight(), random, baseX, baseZ, region);
        generateCacti(random, baseX, baseZ, region);
        generateBones(random, baseX, baseZ, region);
        generateStructures(plugin, random, baseX, baseZ, region);
    }

    private void generateOres(int worldMinY, int worldMaxY, Random random, int baseX, int baseZ, LimitedRegion region) {
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

    private void placeVein(Material mat, int ox, int oy, int oz, int size, Random rng, LimitedRegion region) {
        for (int i = 0; i < size; i++) {
            int bx = ox + rng.nextInt(3) - 1;
            int by = oy + rng.nextInt(3) - 1;
            int bz = oz + rng.nextInt(3) - 1;
            if (!region.isInRegion(bx, by, bz)) continue;
            Material m = region.getType(bx, by, bz);
            if (m == Material.STONE || m == Material.SANDSTONE || m == Material.SAND) {
                region.setType(bx, by, bz, mat);
            }
        }
    }


    private void generateCacti(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(CACTUS_CHANCE) != 0) return;

        int count = 1 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            int cx = baseX + random.nextInt(16);
            int cz = baseZ + random.nextInt(16);
            int cy = findSandSurfaceY(cx, cz, region);
            if (cy < -60) continue;

            int height = 1 + random.nextInt(3);
            for (int dy = 0; dy < height; dy++) {
                if (!region.isInRegion(cx, cy + dy, cz)) continue;
                Material existing = region.getType(cx, cy + dy, cz);
                if (existing == Material.AIR) {
                    region.setType(cx, cy + dy, cz, Material.CACTUS);
                } else break;
            }
        }
    }


    private void generateBones(Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(20) != 0) return;

        int bx = baseX + random.nextInt(16);
        int bz = baseZ + random.nextInt(16);
        int by = findSandSurfaceY(bx, bz, region);
        if (by < -60) return;

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


    private void generateStructures(HexVGPlanets plugin, Random random, int baseX, int baseZ, LimitedRegion region) {
        if (random.nextInt(400) == 0) placeSchematic(plugin, "tatooine_sarlacc", random, baseX, baseZ, region);
        if (random.nextInt(180) == 0) placeSchematic(plugin, "tatooine_hut",     random, baseX, baseZ, region);
        if (random.nextInt(300) == 0) placeSchematic(plugin, "tatooine_base",    random, baseX, baseZ, region);
    }

    private void placeSchematic(HexVGPlanets plugin, String name, Random random, int baseX, int baseZ, LimitedRegion region) {
        Optional<Schematic> opt = plugin.getSchematicRegistry().get(name);
        opt.ifPresent(s -> {
            int sx = baseX + random.nextInt(16);
            int sz = baseZ + random.nextInt(16);
            int sy = findSandSurfaceY(sx, sz, region);
            if (sy <= -60) return;
            SchematicPlacer.placeInRegion(region, SchematicRotator.rotate(s, random.nextInt(4)),
                    sx, sy, sz, plugin.getLootGenerator());
        });
    }
}