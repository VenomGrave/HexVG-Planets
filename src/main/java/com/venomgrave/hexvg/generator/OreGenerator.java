package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

import java.util.Random;


public final class OreGenerator {

    private OreGenerator() {}



    private static final Object[][] ORES_HOTH = {
            { Material.COAL_ORE,        -50,  64,  8, 4 },
            { Material.IRON_ORE,        -60,  30,  6, 3 },
            { Material.GOLD_ORE,        -64,  10,  4, 2 },
            { Material.DIAMOND_ORE,     -64,   2,  3, 1 },
            { Material.LAPIS_ORE,       -64,  20,  5, 2 },
            { Material.EMERALD_ORE,     -64,   8,  2, 1 },
    };


    private static final Object[][] ORES_TATOOINE = {
            { Material.COAL_ORE,        -40,  50,  6, 3 },
            { Material.IRON_ORE,        -60,  20,  5, 2 },
            { Material.GOLD_ORE,        -64,   8,  4, 2 },
            { Material.REDSTONE_ORE,    -64,  16,  5, 2 },
            { Material.COPPER_ORE,      -20,  60,  6, 3 },
    };


    private static final Object[][] ORES_DAGOBAH = {
            { Material.COAL_ORE,        -50,  40,  6, 3 },
            { Material.IRON_ORE,        -60,  20,  5, 2 },
            { Material.CLAY,            -20,  70,  8, 5 },
            { Material.GRAVEL,          -40,  50,  7, 4 },
            { Material.MOSS_BLOCK,       -5,  80,  4, 3 },
    };


    private static final Object[][] ORES_MUSTAFAR = {
            { Material.IRON_ORE,        -60,  80,  5, 3 },
            { Material.GOLD_ORE,        -64,  40,  4, 3 },
            { Material.ANCIENT_DEBRIS,  -64,  16,  2, 1 },
            { Material.NETHER_QUARTZ_ORE,-20, 120, 6, 4 },
            { Material.NETHER_GOLD_ORE, -30,  80,  5, 3 },
            { Material.BLACKSTONE,       20, 140,  8, 5 },
    };


    private static boolean isHostBlock(Material m, PlanetType type) {
        switch (type) {
            case HOTH:
                return m == Material.STONE || m == Material.PACKED_ICE || m == Material.ICE;
            case TATOOINE:
                return m == Material.STONE || m == Material.SANDSTONE || m == Material.SAND;
            case DAGOBAH:
                return m == Material.STONE || m == Material.DIRT || m == Material.GRASS_BLOCK;
            case MUSTAFAR:
                return m == Material.STONE || m == Material.BASALT || m == Material.BLACKSTONE;
            default:
                return m == Material.STONE;
        }
    }


    public static Runnable taskFor(HexVGPlanets plugin, World world,
                                   int chunkX, int chunkZ, PlanetType type) {
        return () -> generate(world, chunkX, chunkZ, type, new Random(
                (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ world.getSeed()
        ));
    }


    public static void generate(World world, int chunkX, int chunkZ, PlanetType type, Random rng) {
        if (!ConfigManager.isGenerateOres(HexVGPlanets.getInstance(), world)) return;

        Object[][] ores = getOresFor(type);
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (Object[] ore : ores) {
            Material mat = (Material) ore[0];
            int oreMinY  = Math.max(worldMinY, (int) ore[1]);
            int oreMaxY  = Math.min(worldMaxY - 1, (int) ore[2]);
            int veinSize = (int) ore[3];
            int veins    = (int) ore[4];

            for (int v = 0; v < veins; v++) {
                int ox = baseX + rng.nextInt(16);
                int oz = baseZ + rng.nextInt(16);
                int oy = oreMinY + rng.nextInt(Math.max(1, oreMaxY - oreMinY));
                placeVein(world, mat, ox, oy, oz, veinSize, type, rng);
            }
        }
    }



    private static Object[][] getOresFor(PlanetType type) {
        switch (type) {
            case TATOOINE: return ORES_TATOOINE;
            case DAGOBAH:  return ORES_DAGOBAH;
            case MUSTAFAR: return ORES_MUSTAFAR;
            default:       return ORES_HOTH;
        }
    }

    private static void placeVein(World world, Material mat, int ox, int oy, int oz, int size, PlanetType type, Random rng) {
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        for (int i = 0; i < size; i++) {
            int bx = ox + rng.nextInt(3) - 1;
            int by = oy + rng.nextInt(3) - 1;
            int bz = oz + rng.nextInt(3) - 1;

            if (by < minY || by >= maxY) continue;
            if (!world.isChunkLoaded(bx >> 4, bz >> 4)) continue;

            Material existing = world.getBlockAt(bx, by, bz).getType();
            if (isHostBlock(existing, type)) {
                world.getBlockAt(bx, by, bz).setType(mat, false);
            }
        }
    }
}