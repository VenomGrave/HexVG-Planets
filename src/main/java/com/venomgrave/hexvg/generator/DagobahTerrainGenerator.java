package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.generator.StructureQueue;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;


public class DagobahTerrainGenerator extends AbstractTerrainGenerator {


    private static final int WATER_TABLE_OFFSET = 4;

    public DagobahTerrainGenerator(HexVGPlanets plugin, WorldInfo worldInfo) {
        super(plugin, worldInfo);
    }

    @Override
    public void generate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        Random localRng = new Random((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L + worldInfo.getSeed());

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                generateColumn(chunk, localRng, random, chunkX * 16 + x, chunkZ * 16 + z, x, z);
            }
        }

        queueStructures(worldInfo, random, chunkX, chunkZ);
    }

    private void generateColumn(ChunkData chunk, Random localRng, Random chunkRng, int rx, int rz, int x, int z) {
        placeBedrock(chunk, x, z, localRng);
        int y = minY + 6;


        int solidTop = minY + 27 + surfaceOffset;
        fill(chunk, x, z, y, solidTop, Material.STONE);
        y = solidTop + 1;


        y += (int)(noise.noise(rx, rz, 8, 16) * 3.0);


        int dirtH = (int)(noise.noise(rx, rz, 8, 11) * 5.0);
        if (dirtH > 2) { fill(chunk, x, z, y, y + dirtH - 2, Material.DIRT); y += dirtH - 2; }


        int gravelH = (int)(noise.noise(rx, rz, 7, 16) * 5.0);
        if (gravelH > 2) { fill(chunk, x, z, y, y + gravelH - 2, Material.GRAVEL); y += gravelH - 2; }


        int clayH = 1 + (int)(noise.noise(rx, rz, 3, 9) * 5.0);
        if (clayH > 3) {
            chunk.setBlock(x, y, z, Material.TERRACOTTA); y++;
            fill(chunk, x, z, y, y + clayH - 4, Material.CLAY); y += clayH - 4;
        }


        double iceH = noise.noise(rx, rz, 4, 63) * 2.0 + noise.noise(rx, rz, 10, 12);
        int surface = seaY + surfaceOffset + (int)(iceH * 2.5);

        fill(chunk, x, z, y, surface, Material.DIRT);
        y = surface + 1;


        double doMtn = noise.noise(rx, rz, 4, 336) * 20.0;
        if (doMtn > 10.0) {
            double mf = (doMtn - 10.0) / 10.0;
            int mh = (int)((noise.noise(rx, rz, 4, 87) * 84 + noise.noise(rx, rz, 8, 30) * 15) * mf);
            int mb = minY + 26 + surfaceOffset;
            for (int i = 0; i < mh; i++) {
                int my = mb + i;
                if (my < maxY) { chunk.setBlock(x, my, z, Material.STONE); if (my > y) y = my; }
            }
        }


        int coverH = 1 + (int)(noise.noise(rx, rz, 8, 76) * 2.0);
        for (int i = 0; i < coverH; i++) {
            if (y < maxY) {
                chunk.setBlock(x, y, z, i == coverH - 1 ? Material.GRASS_BLOCK : Material.DIRT);
                y++;
            }
        }


        double caveA = noise.noise(rx, rz, 2, 150) * 4.0 + noise.noise(rx, rz, 2, 43) * 4.0;
        for (int i = minY + 4; i < minY + 128 + surfaceOffset; i++) {
            double d = noise.noise(rx, i, rz, 4, 10) * 16.0;
            double a = caveA;
            if (i > seaY + surfaceOffset + 32) a += 8.0 * ((i - (seaY + surfaceOffset + 32.0)) / 32.0);
            if (d > a + 8) {
                Material old = chunk.getType(x, i, z);
                if (old == Material.STONE || old == Material.DIRT || old == Material.GRASS_BLOCK) {
                    chunk.setBlock(x, i, z, i < minY + 12 ? Material.LAVA : Material.AIR);
                }
            }
        }


        int waterTableY = seaY + WATER_TABLE_OFFSET;
        if (y < waterTableY) {
            double sediment = noise.noise(rx, rz, 4, 23) * 100.0;
            int sedDepth = (int)(noise.noise(rx, rz, 4, 18) * 2.0) + 1;
            Material sedMat = Material.DIRT;
            if      (sediment > 90) sedMat = Material.SAND;
            else if (sediment > 80) sedMat = Material.CLAY;
            else if (sediment > 70) sedMat = Material.GRAVEL;
            else if (sediment > 60) sedMat = Material.TERRACOTTA;
            else if (sediment <  10) sedMat = Material.STONE;
            for (int i = 1; i <= sedDepth && y - i > minY; i++) {
                chunk.setBlock(x, y - i, z, sedMat);
            }
        }


        int wy = waterTableY;
        boolean hasWater = false;
        while (wy > minY && chunk.getType(x, wy, z) == Material.AIR) {
            chunk.setBlock(x, wy, z, Material.WATER);
            wy--;
            hasWater = true;
        }


        if (hasWater) {
            double lily = 1 + noise.noise(rx, rz, 4, 17) * 100.0;
            if (chunkRng.nextInt((int) lily + 1) == 1 && waterTableY + 1 < maxY) {
                chunk.setBlock(x, waterTableY + 1, z, Material.LILY_PAD);
            }
        }


        double doLava = noise.noise(rx, rz, 4, 71) * 10.0;
        if (doLava > 7.0) {
            int lDep = (int)(noise.noise(rx, rz, 4, 7) * 21.0) - 18;
            if (lDep > -2) {
                int ls = minY + 8 - (2 + lDep) / 2;
                for (int i = -1; i < lDep; i++) {
                    int ly = ls + i;
                    if (ly < maxY) chunk.setBlock(x, ly, z, ly < minY + 7 ? Material.LAVA : Material.AIR);
                }
            }
        }
    }

    private void queueStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {

        StructureQueue.queue(worldInfo, random, chunkX, chunkZ, com.venomgrave.hexvg.world.PlanetType.DAGOBAH);

        if (random.nextInt(DagobahHugeTreeGenerator.DEFAULT_RARITY) == 0) {
            final long treeSeed = (long) chunkX * 341873128712L
                    ^ (long) chunkZ * 132897987541L
                    ^ worldInfo.getSeed() ^ 0xDAB0B0L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                World world = Bukkit.getWorld(worldInfo.getName());
                if (world == null || !world.isChunkLoaded(chunkX, chunkZ)) return;
                DagobahHugeTreeGenerator.taskFor(world, chunkX, chunkZ,
                        new Random(treeSeed)).run();
            }, 4L);
        }
    }
}