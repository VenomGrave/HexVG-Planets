package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public final class OreGenerator {

    private OreGenerator() {}

    // ---------------------------
    // HARDCORE PLANET ORE TABLES
    // mat, minY, maxY, veinSize, veinsPerChunk
    // ---------------------------

    private static final Object[][] ORES_HOTH = {
            { Material.DIAMOND_ORE,   -64,  20, 3, 1 },
            { Material.LAPIS_ORE,     -40,  40, 4, 2 },
            { Material.EMERALD_ORE,   -30,  60, 2, 1 },
            { Material.IRON_ORE,      -64,  40, 5, 2 },
    };

    private static final Object[][] ORES_TATOOINE = {
            { Material.COPPER_ORE,    -20,  60, 6, 3 },
            { Material.GOLD_ORE,      -64,  20, 4, 2 },
            { Material.REDSTONE_ORE,  -64,  16, 5, 2 },
            { Material.QUARTZ_BLOCK,  -30,  60, 6, 3 },
    };

    private static final Object[][] ORES_DAGOBAH = {
            { Material.CLAY,          -20,  70, 8, 5 },
            { Material.MOSS_BLOCK,    -10,  80, 4, 3 },
            { Material.GRAVEL,        -40,  50, 7, 4 },
            { Material.IRON_ORE,      -60,  20, 5, 2 },
    };

    private static final Object[][] ORES_MUSTAFAR = {
            { Material.ANCIENT_DEBRIS,    -64,  16, 2, 1 },
            { Material.NETHER_QUARTZ_ORE, -20, 120, 6, 4 },
            { Material.NETHER_GOLD_ORE,   -30,  80, 5, 3 },
            { Material.BLACKSTONE,         10, 140, 8, 5 },
            { Material.BASALT,             10, 140, 8, 5 },
    };

    // ---------------------------
    // HOST BLOCKS PER PLANET
    // ---------------------------

    private static boolean isHostBlock(Material m, PlanetType type) {
        return switch (type) {
            case HOTH ->
                    m == Material.STONE || m == Material.ICE || m == Material.PACKED_ICE || m == Material.SNOW_BLOCK;
            case TATOOINE ->
                    m == Material.STONE || m == Material.SANDSTONE || m == Material.SAND;
            case DAGOBAH ->
                    m == Material.STONE || m == Material.DIRT || m == Material.GRASS_BLOCK || m == Material.MUD;
            case MUSTAFAR ->
                    m == Material.BLACKSTONE || m == Material.BASALT || m == Material.MAGMA_BLOCK;
            default -> m == Material.STONE;
        };
    }

    // ---------------------------
    // ENTRY POINT (jak w oryginale)
    // ---------------------------

    public static Runnable taskFor(HexVGPlanets plugin, World world,
                                   int chunkX, int chunkZ, PlanetType type) {
        long seed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ world.getSeed();
        Random rng = new Random(seed);
        return () -> generate(world, chunkX, chunkZ, type, rng);
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

            if (oreMaxY <= oreMinY) continue;

            for (int v = 0; v < veins; v++) {
                int ox = baseX + rng.nextInt(16);
                int oz = baseZ + rng.nextInt(16);
                int oy = oreMinY + rng.nextInt(Math.max(1, oreMaxY - oreMinY));
                placeVein(world, mat, ox, oy, oz, veinSize, type, rng);
            }
        }
    }

    private static Object[][] getOresFor(PlanetType type) {
        return switch (type) {
            case TATOOINE -> ORES_TATOOINE;
            case DAGOBAH  -> ORES_DAGOBAH;
            case MUSTAFAR -> ORES_MUSTAFAR;
            default       -> ORES_HOTH;
        };
    }

    // ---------------------------
    // VEIN PLACEMENT (świadomie na World, jak w oryginale)
    // ---------------------------

    private static void placeVein(World world, Material mat,
                                  int ox, int oy, int oz,
                                  int size, PlanetType type, Random rng) {

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
