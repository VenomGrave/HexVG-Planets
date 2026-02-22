package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.generator.StructureQueue;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;


public class TatooineTerrainGenerator extends AbstractTerrainGenerator {

    public TatooineTerrainGenerator(HexVGPlanets plugin, WorldInfo worldInfo) {
        super(plugin, worldInfo);
    }

    @Override
    public void generate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        Random localRng = new Random((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L + worldInfo.getSeed());

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                generateColumn(chunk, localRng, chunkX * 16 + x, chunkZ * 16 + z, x, z);
            }
        }

        queueStructures(worldInfo, random, chunkX, chunkZ);
    }

    private void generateColumn(ChunkData chunk, Random rng, int rx, int rz, int x, int z) {

        placeBedrock(chunk, x, z, rng);
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


        double iceHeight = noise.noise(rx, rz, 4, 63) * 2.0 + noise.noise(rx, rz, 10, 12);
        double ice       = iceHeight * 2.5;
        int    surface   = seaY + surfaceOffset + (int) ice;

        fill(chunk, x, z, y, surface - (int)(noise.noise(rx, rz, 3, 7) * 15), Material.SANDSTONE);
        y = surface - (int)(noise.noise(rx, rz, 3, 7) * 15) + 1;
        fill(chunk, x, z, y, surface, Material.SAND);
        y = surface + 1;


        double doMtn = noise.noise(rx, rz, 4, 236) * 20.0;
        if (doMtn > 16.0) {
            double mf = doMtn > 18.0 ? 1.0 : (doMtn - 16.0) * 0.5;
            int mh = (int)((noise.noise(rx, rz, 4, 27) * 84 + noise.noise(rx, rz, 8, 3) * 5) * mf);
            int mb = minY + 26 + surfaceOffset;
            for (int i = 0; i < mh; i++) {
                int my = mb + i;
                if (my < maxY) { chunk.setBlock(x, my, z, Material.STONE); if (my > y) y = my; }
            }
        }


        int sandCover = 1 + (int)(noise.noise(rx, rz, 8, 76) * 2.0);
        fill(chunk, x, z, y, y + sandCover, Material.SAND);
        y += sandCover;


        double caveBase = noise.noise(rx, rz, 2, 60) * 8.0;
        for (int i = minY + 4; i < minY + 128 + surfaceOffset; i++) {
            double d = noise.noise(rx, i, rz, 4, 8) * 16.0;
            double a = caveBase;
            if (i > seaY + surfaceOffset + 32) a += 8.0 * ((i - (seaY + surfaceOffset + 32.0)) / 32.0);
            if (d > a + 10) {
                Material old = chunk.getType(x, i, z);
                if (old == Material.STONE || old == Material.SANDSTONE || old == Material.SAND) {
                    chunk.setBlock(x, i, z, i < minY + 12 ? Material.LAVA : Material.AIR);
                }
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


        replaceTopBlock(chunk, x, z, Material.SANDSTONE, Material.SAND, maxY);
    }

    private void replaceTopBlock(ChunkData chunk, int x, int z, Material from, Material to, int maxY) {
        for (int y = maxY - 1; y > minY; y--) {
            Material m = chunk.getType(x, y, z);
            if (m != Material.AIR) {
                if (m == from) chunk.setBlock(x, y, z, to);
                break;
            }
        }
    }

    private void queueStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        StructureQueue.queue(worldInfo, random, chunkX, chunkZ,
                com.venomgrave.hexvg.world.PlanetType.TATOOINE);
    }
}