package com.venomgrave.hexvg.generator;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;


public final class CaveGenerator {

    private CaveGenerator() {}

    private static final int[][] PARAMS_HOTH     = {{ 1, 20, 1, 2, -40, 40  }};
    private static final int[][] PARAMS_TATOOINE = {{ 2, 40, 1, 2, -50, 30  }};
    private static final int[][] PARAMS_DAGOBAH  = {{ 3, 30, 1, 3, -30, 50  }};
    private static final int[][] PARAMS_MUSTAFAR = {{ 2, 15, 1, 2, -60, 20  }};


    private static final Material[] FILL_HOTH     = { Material.AIR, Material.WATER };
    private static final Material[] FILL_TATOOINE = { Material.AIR };
    private static final Material[] FILL_DAGOBAH  = { Material.AIR, Material.AIR, Material.WATER };
    private static final Material[] FILL_MUSTAFAR = { Material.AIR, Material.LAVA };

    public static Runnable taskFor(World world, int chunkX, int chunkZ, com.venomgrave.hexvg.world.PlanetType type) {
        return () -> generate(world, chunkX, chunkZ, type, new Random(
                (long) chunkX * 341873128712L ^ (long) chunkZ * 132897987541L ^ world.getSeed() ^ 0xCAFEL
        ));
    }


    public static void generate(World world, int chunkX, int chunkZ, com.venomgrave.hexvg.world.PlanetType type, Random rng) {
        int[][] params = getParams(type);
        Material[] fills = getFills(type);

        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;
        int worldMinY = world.getMinHeight();
        int worldMaxY = world.getMaxHeight();

        for (int[] p : params) {
            int worms   = p[0];
            int length  = p[1];
            int minR    = p[2];
            int maxR    = p[3];
            int minY    = Math.max(worldMinY + 4, p[4]);
            int maxY    = Math.min(worldMaxY - 4, p[5]);

            for (int w = 0; w < worms; w++) {

                double wx = baseX + 4 + rng.nextInt(8);
                double wy = minY  + rng.nextInt(Math.max(1, maxY - minY));
                double wz = baseZ + 4 + rng.nextInt(8);


                double yaw   = rng.nextDouble() * Math.PI * 2;
                double pitch = (rng.nextDouble() - 0.5) * Math.PI * 0.5;

                Material fill = fills[rng.nextInt(fills.length)];

                for (int step = 0; step < length; step++) {

                    yaw   += (rng.nextDouble() - 0.5) * 0.4;
                    pitch += (rng.nextDouble() - 0.5) * 0.2;
                    pitch  = Math.max(-Math.PI * 0.3, Math.min(Math.PI * 0.3, pitch));

                    double cos = Math.cos(pitch);
                    wx += Math.sin(yaw) * cos;
                    wy += Math.sin(pitch);
                    wz += Math.cos(yaw) * cos;

                    int ix = (int) Math.round(wx);
                    int iy = (int) Math.round(wy);
                    int iz = (int) Math.round(wz);

                    if (iy < minY || iy > maxY) break;

                    int radius = minR + rng.nextInt(maxR - minR + 1);
                    carveOrb(world, ix, iy, iz, radius, fill, type);
                }
            }
        }
    }


    private static void carveOrb(World world, int cx, int cy, int cz, int radius, Material fill, com.venomgrave.hexvg.world.PlanetType type) {
        int minY = world.getMinHeight() + 2;
        int maxY = world.getMaxHeight() - 2;
        double r2 = (double) radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx*dx + dy*dy + dz*dz > r2) continue;

                    int bx = cx + dx;
                    int by = cy + dy;
                    int bz = cz + dz;

                    if (by < minY || by >= maxY) continue;
                    if (!world.isChunkLoaded(bx >> 4, bz >> 4)) continue;

                    Material existing = world.getBlockAt(bx, by, bz).getType();
                    if (!isCarveTarget(existing, type)) continue;

                    world.getBlockAt(bx, by, bz).setType(fill, false);
                }
            }
        }
    }


    private static boolean isCarveTarget(Material m, com.venomgrave.hexvg.world.PlanetType type) {
        if (m.isAir()) return false;
        if (m == Material.BEDROCK) return false;
        if (m == Material.LAVA && type != com.venomgrave.hexvg.world.PlanetType.MUSTAFAR) return false;
        switch (type) {
            case HOTH:
                return m == Material.STONE || m == Material.ICE
                        || m == Material.PACKED_ICE || m == Material.SNOW_BLOCK
                        || m == Material.DIRT || m == Material.GRAVEL;
            case TATOOINE:
                return m == Material.STONE || m == Material.SANDSTONE
                        || m == Material.SAND || m == Material.GRAVEL;
            case DAGOBAH:
                return m == Material.STONE || m == Material.DIRT
                        || m == Material.GRASS_BLOCK || m == Material.GRAVEL
                        || m == Material.CLAY;
            case MUSTAFAR:
                return m == Material.STONE || m == Material.BASALT
                        || m == Material.BLACKSTONE || m == Material.COBBLESTONE;
            default:
                return m == Material.STONE;
        }
    }


    private static int[][] getParams(com.venomgrave.hexvg.world.PlanetType type) {
        switch (type) {
            case TATOOINE: return PARAMS_TATOOINE;
            case DAGOBAH:  return PARAMS_DAGOBAH;
            case MUSTAFAR: return PARAMS_MUSTAFAR;
            default:       return PARAMS_HOTH;
        }
    }

    private static Material[] getFills(com.venomgrave.hexvg.world.PlanetType type) {
        switch (type) {
            case TATOOINE: return FILL_TATOOINE;
            case DAGOBAH:  return FILL_DAGOBAH;
            case MUSTAFAR: return FILL_MUSTAFAR;
            default:       return FILL_HOTH;
        }
    }
}