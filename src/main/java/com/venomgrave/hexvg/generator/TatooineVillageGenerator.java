package com.venomgrave.hexvg.generator;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;


public final class TatooineVillageGenerator {

    private TatooineVillageGenerator() {}

    public static final int DEFAULT_RARITY = 180;



    public static Runnable taskFor(World world, int chunkX, int chunkZ, Random rng) {
        final long seed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ world.getSeed() ^ 0xA3A10L;
        final int bx = chunkX * 16 + 3 + rng.nextInt(10);
        final int bz = chunkZ * 16 + 3 + rng.nextInt(10);

        return () -> {
            if (!world.isChunkLoaded(chunkX, chunkZ)) return;
            int by = world.getHighestBlockYAt(bx, bz);
            generate(world, bx, by, bz, new Random(seed));
        };
    }



    public static void generate(World world, int ox, int oy, int oz, Random rng) {

        flattenArea(world, ox, oy, oz, 20);


        buildWell(world, ox + 10, oy, oz + 10);

        int hutCount = 3 + rng.nextInt(4);
        int[][] hutOffsets = {
                {0, 0}, {18, 0}, {0, 18}, {18, 18}, {9, 0}, {0, 9}
        };
        for (int i = 0; i < hutCount; i++) {
            int hx = ox + hutOffsets[i][0];
            int hz = oz + hutOffsets[i][1];
            int hy = world.getHighestBlockYAt(hx, hz);
            buildHut(world, hx, hy, hz, rng);
        }


        int stallCount = 1 + rng.nextInt(3);
        for (int i = 0; i < stallCount; i++) {
            int sx = ox + 5 + rng.nextInt(12);
            int sz = oz + 5 + rng.nextInt(12);
            int sy = world.getHighestBlockYAt(sx, sz);
            buildStall(world, sx, sy, sz, rng);
        }


        if (rng.nextInt(3) == 0) {
            buildWreckage(world, ox + 2 + rng.nextInt(16), oy, oz + 2 + rng.nextInt(16), rng);
        }


        buildLampPost(world, ox + 8,  oy, oz + 10);
        buildLampPost(world, ox + 12, oy, oz + 10);
    }



    private static void buildWell(World world, int ox, int oy, int oz) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean wall = dx == -1 || dx == 1 || dz == -1 || dz == 1;
                if (wall) {
                    setBlock(world, ox + dx, oy,     oz + dz, Material.SANDSTONE);
                    setBlock(world, ox + dx, oy + 1, oz + dz, Material.SMOOTH_SANDSTONE);
                } else {
                    setBlock(world, ox + dx, oy,     oz + dz, Material.WATER);
                    setBlock(world, ox + dx, oy - 1, oz + dz, Material.WATER);
                }
            }
        }

        setBlock(world, ox, oy + 2, oz - 1, Material.OAK_FENCE);
        setBlock(world, ox, oy + 3, oz - 1, Material.OAK_FENCE);
        setBlock(world, ox - 1, oy + 3, oz - 1, Material.OAK_FENCE);
        setBlock(world, ox + 1, oy + 3, oz - 1, Material.OAK_FENCE);
    }



    private static void buildHut(World world, int ox, int oy, int oz, Random rng) {
        int w = 5 + rng.nextInt(3);
        int h = 3 + rng.nextInt(2);


        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < w; dz++) {
                setBlock(world, ox + dx, oy - 1, oz + dz, Material.SMOOTH_SANDSTONE);
            }
        }


        for (int dx = 0; dx < w; dx++) {
            for (int dy = 0; dy <= h; dy++) {
                Material mat = rng.nextInt(5) == 0 ? Material.CHISELED_SANDSTONE
                        : rng.nextInt(4) == 0 ? Material.SMOOTH_SANDSTONE
                        : Material.SANDSTONE;
                setBlock(world, ox + dx, oy + dy, oz,         mat);
                setBlock(world, ox + dx, oy + dy, oz + w - 1, mat);
            }
        }
        for (int dz = 0; dz < w; dz++) {
            for (int dy = 0; dy <= h; dy++) {
                Material mat = rng.nextInt(4) == 0 ? Material.SMOOTH_SANDSTONE : Material.SANDSTONE;
                setBlock(world, ox,         oy + dy, oz + dz, mat);
                setBlock(world, ox + w - 1, oy + dy, oz + dz, mat);
            }
        }

        // Dach płaski z SAND i SANDSTONE_SLAB
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < w; dz++) {
                setBlock(world, ox + dx, oy + h + 1, oz + dz,
                        rng.nextInt(4) == 0 ? Material.SAND : Material.SMOOTH_SANDSTONE);
            }
        }

        // Drzwi (środek przedniej ściany)
        int door = ox + w / 2;
        clearBlock(world, door, oy,     oz);
        clearBlock(world, door, oy + 1, oz);

        // Okno
        clearBlock(world, ox + 1, oy + 1, oz + w - 1);

        // Wnętrze – skrzynka, łóżko, stół
        clearInterior(world, ox, oy, oz, w, h);
        setBlock(world, ox + 1, oy, oz + 1, Material.CHEST);
        setBlock(world, ox + w - 2, oy, oz + 1, Material.CRAFTING_TABLE);
    }

    private static void clearInterior(World world, int ox, int oy, int oz, int w, int h) {
        for (int dx = 1; dx < w - 1; dx++) {
            for (int dy = 0; dy <= h; dy++) {
                for (int dz = 1; dz < w - 1; dz++) {
                    clearBlock(world, ox + dx, oy + dy, oz + dz);
                }
            }
        }
    }

    // ── Stragan targowy ───────────────────────────────────────────────

    private static void buildStall(World world, int ox, int oy, int oz, Random rng) {
        // 4 słupy
        for (int[] pos : new int[][]{{0,0},{3,0},{0,3},{3,3}}) {
            setBlock(world, ox + pos[0], oy,     oz + pos[1], Material.OAK_LOG);
            setBlock(world, ox + pos[0], oy + 1, oz + pos[1], Material.OAK_LOG);
            setBlock(world, ox + pos[0], oy + 2, oz + pos[1], Material.OAK_LOG);
        }
        // Dach z dywanu (markiza)
        Material awning = rng.nextBoolean() ? Material.ORANGE_CARPET : Material.BROWN_CARPET;
        for (int dx = 0; dx <= 3; dx++) {
            for (int dz = 0; dz <= 3; dz++) {
                setBlock(world, ox + dx, oy + 3, oz + dz, awning);
            }
        }
        // Stół ze "sprzedawanym towarem"
        setBlock(world, ox + 1, oy, oz + 1, Material.OAK_SLAB);
        setBlock(world, ox + 2, oy, oz + 1, Material.OAK_SLAB);
        // Losowy towar na stole
        Material[] goods = {Material.IRON_INGOT, Material.GOLD_INGOT, Material.BONE_BLOCK,
                Material.SAND, Material.GLASS};
        setBlock(world, ox + 1, oy + 1, oz + 1, goods[rng.nextInt(goods.length)]);
    }

    // ── Wrak statku / złom ────────────────────────────────────────────

    private static void buildWreckage(World world, int ox, int oy, int oz, Random rng) {
        int size = 4 + rng.nextInt(5);
        // Masa żeliwa i kamienia w losowym kształcie
        for (int i = 0; i < size * size; i++) {
            int dx = rng.nextInt(size) - size / 2;
            int dy = rng.nextInt(3) - 1;
            int dz = rng.nextInt(size) - size / 2;
            Material mat;
            switch (rng.nextInt(5)) {
                case 0: mat = Material.IRON_BARS;      break;
                case 1: mat = Material.STONE_BRICKS;   break;
                case 2: mat = Material.GRAVEL;          break;
                case 3: mat = Material.COBBLESTONE;     break;
                default: mat = Material.IRON_BLOCK;     break;
            }
            setBlock(world, ox + dx, oy + dy, oz + dz, mat);
        }
    }

    // ── Słup z latarnią ───────────────────────────────────────────────

    private static void buildLampPost(World world, int ox, int oy, int oz) {
        setBlock(world, ox, oy,     oz, Material.SMOOTH_SANDSTONE);
        setBlock(world, ox, oy + 1, oz, Material.OAK_FENCE);
        setBlock(world, ox, oy + 2, oz, Material.OAK_FENCE);
        setBlock(world, ox, oy + 3, oz, Material.OAK_FENCE);
        setBlock(world, ox, oy + 4, oz, Material.LANTERN);
    }

    // ── Wyrównanie terenu ─────────────────────────────────────────────

    private static void flattenArea(World world, int ox, int oy, int oz, int size) {
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                int x = ox + dx, z = oz + dz;
                if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;
                int surfY = world.getHighestBlockYAt(x, z);
                if (surfY == oy) continue;
                if (surfY > oy) {
                    // Zbyt wysoko – zdejmij nadmiar
                    for (int y = oy + 1; y <= surfY; y++) {
                        world.getBlockAt(x, y, z).setType(Material.AIR, false);
                    }
                    world.getBlockAt(x, oy, z).setType(Material.SANDSTONE, false);
                } else {
                    // Zbyt nisko – wypełnij piaskowcem
                    for (int y = surfY; y <= oy; y++) {
                        world.getBlockAt(x, y, z).setType(Material.SANDSTONE, false);
                    }
                }
            }
        }
    }



    private static void setBlock(World world, int x, int y, int z, Material mat) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return;
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) return;
        world.getBlockAt(x, y, z).setType(mat, false);
    }

    private static void clearBlock(World world, int x, int y, int z) {
        setBlock(world, x, y, z, Material.AIR);
    }
}