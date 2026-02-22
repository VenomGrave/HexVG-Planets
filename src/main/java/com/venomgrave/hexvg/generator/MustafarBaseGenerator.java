package com.venomgrave.hexvg.generator;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;


public final class MustafarBaseGenerator {

    private MustafarBaseGenerator() {}

    public static final int DEFAULT_RARITY = 400;


    public static Runnable taskFor(World world, int chunkX, int chunkZ, Random rng) {
        final long seed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ world.getSeed() ^ 0x517B453L;
        final int bx = chunkX * 16 + 3 + rng.nextInt(10);
        final int bz = chunkZ * 16 + 3 + rng.nextInt(10);

        return () -> {
            if (!world.isChunkLoaded(chunkX, chunkZ)) return;
            int by = findSolidY(world, bx, bz);
            if (by <= world.getMinHeight() + 5) return;
            generate(world, bx, by, bz, new Random(seed));
        };
    }


    public static void generate(World world, int ox, int oy, int oz, Random rng) {
        int w = 17;
        int h = 12;

        buildFoundation(world, ox, oy, oz, w);
        buildWalls(world, ox, oy, oz, w, h, rng);
        buildTowers(world, ox, oy, oz, w, h, rng);
        buildInterior(world, ox, oy, oz, w, h);
        buildBridges(world, ox, oy, oz, w, rng);
        addLighting(world, ox, oy, oz, w, h);
    }


    private static void buildFoundation(World world, int ox, int oy, int oz, int w) {
        for (int dx = -1; dx <= w + 1; dx++) {
            for (int dz = -1; dz <= w + 1; dz++) {
                setBlock(world, ox + dx, oy - 1, oz + dz, Material.BLACKSTONE);
                setBlock(world, ox + dx, oy - 2, oz + dz, Material.BASALT);
            }
        }
    }


    private static void buildWalls(World world, int ox, int oy, int oz,
                                   int w, int h, Random rng) {
        for (int dy = 0; dy <= h; dy++) {
            for (int i = 0; i <= w; i++) {
                Material mat = wallMat(dy, h, rng);
                setBlock(world, ox + i, oy + dy, oz,     mat);
                setBlock(world, ox + i, oy + dy, oz + w, mat);
                setBlock(world, ox,     oy + dy, oz + i, mat);
                setBlock(world, ox + w, oy + dy, oz + i, mat);
            }
        }


        int gate = ox + w / 2;
        for (int dz2 = -1; dz2 <= 1; dz2++) {
            for (int dy = 0; dy <= 3; dy++) {
                clearBlock(world, gate + dz2, oy + dy, oz);
            }
        }


        for (int i = 0; i <= w; i += 2) {
            setBlock(world, ox + i, oy + h + 1, oz,     Material.POLISHED_BLACKSTONE);
            setBlock(world, ox + i, oy + h + 1, oz + w, Material.POLISHED_BLACKSTONE);
            setBlock(world, ox,     oy + h + 1, oz + i, Material.POLISHED_BLACKSTONE);
            setBlock(world, ox + w, oy + h + 1, oz + i, Material.POLISHED_BLACKSTONE);
        }
    }

    private static Material wallMat(int dy, int maxH, Random rng) {
        if (dy == 0 || dy == maxH) return Material.POLISHED_BLACKSTONE;
        return rng.nextInt(7) == 0 ? Material.CRACKED_NETHER_BRICKS
                : rng.nextInt(4) == 0 ? Material.NETHER_BRICKS
                : Material.BLACKSTONE;
    }



    private static void buildTowers(World world, int ox, int oy, int oz,
                                    int w, int h, Random rng) {
        int[][] corners = {{0, 0}, {0, w}, {w, 0}, {w, w}};
        for (int[] c : corners) {
            buildTower(world, ox + c[0], oy, oz + c[1], h + 4, rng);
        }
    }

    private static void buildTower(World world, int tx, int ty, int tz,
                                   int height, Random rng) {
        int r = 2;
        for (int dy = 0; dy <= height; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    boolean isWall = dx == -r || dx == r || dz == -r || dz == r;
                    if (!isWall) continue;
                    setBlock(world, tx + dx, ty + dy, tz + dz,
                            rng.nextInt(5) == 0 ? Material.OBSIDIAN : Material.BLACKSTONE);
                }
            }
        }

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                setBlock(world, tx + dx, ty + height + 1, tz + dz, Material.OBSIDIAN);
            }
        }

        setBlock(world, tx, ty + height + 2, tz, Material.SOUL_LANTERN);
    }


    private static void buildInterior(World world, int ox, int oy, int oz, int w, int h) {
        // Oczyść wnętrze (zostawiamy podłogę)
        for (int dx = 1; dx < w; dx++) {
            for (int dy = 0; dy <= h; dy++) {
                for (int dz = 1; dz < w; dz++) {
                    if (dy == 0) {
                        setBlock(world, ox + dx, oy + dy, oz + dz, Material.POLISHED_BLACKSTONE);
                    } else {
                        clearBlock(world, ox + dx, oy + dy, oz + dz);
                    }
                }
            }
        }

        int mid = w / 2;


        for (int dx = mid - 2; dx <= mid + 2; dx++) {
            for (int dz = mid - 2; dz <= mid + 2; dz++) {
                setBlock(world, ox + dx, oy + 1, oz + dz, Material.CHISELED_NETHER_BRICKS);
            }
        }
        setBlock(world, ox + mid, oy + 2, oz + mid, Material.LODESTONE); // tron
        setBlock(world, ox + mid, oy + 3, oz + mid, Material.ENCHANTING_TABLE);


        setBlock(world, ox + 2,     oy + 1, oz + 2,     Material.CHEST);
        setBlock(world, ox + w - 2, oy + 1, oz + 2,     Material.CHEST);
        setBlock(world, ox + 2,     oy + 1, oz + w - 2, Material.CHEST);
        setBlock(world, ox + w - 2, oy + 1, oz + w - 2, Material.CHEST);


        for (int[] pos : new int[][]{{mid - 3, mid}, {mid + 3, mid}, {mid, mid - 3}, {mid, mid + 3}}) {
            setBlock(world, ox + pos[0], oy + 1, oz + pos[1], Material.SOUL_SAND);
            setBlock(world, ox + pos[0], oy + 2, oz + pos[1], Material.SOUL_FIRE);
        }
    }



    private static void buildBridges(World world, int ox, int oy, int oz, int w, Random rng) {
        int gate = ox + w / 2;
        for (int i = 1; i <= 10; i++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (!world.isChunkLoaded((gate + dx) >> 4, (oz - i) >> 4)) continue;
                setBlock(world, gate + dx, oy - 1, oz - i, Material.OBSIDIAN);
                if (dx == -1 || dx == 1) {
                    setBlock(world, gate + dx, oy, oz - i, Material.NETHER_BRICK_FENCE);
                }
            }
        }
    }


    private static void addLighting(World world, int ox, int oy, int oz, int w, int h) {for (int i = 2; i < w; i += 4) {
            setBlock(world, ox + i, oy + h - 1, oz + 1,     Material.SOUL_TORCH);
            setBlock(world, ox + i, oy + h - 1, oz + w - 1, Material.SOUL_TORCH);
            setBlock(world, ox + 1,     oy + h - 1, oz + i, Material.SOUL_TORCH);
            setBlock(world, ox + w - 1, oy + h - 1, oz + i, Material.SOUL_TORCH);
        }
    }


    private static int findSolidY(World world, int x, int z) {
        for (int y = world.getMaxHeight() - 1; y > world.getMinHeight(); y--) {
            if (!world.isChunkLoaded(x >> 4, z >> 4)) return world.getMinHeight();
            Material m = world.getBlockAt(x, y, z).getType();
            if (!m.isAir() && m != Material.LAVA && m != Material.WATER) return y + 1;
        }
        return world.getMinHeight();
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