package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.generator.StructureQueue;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class MustafarTerrainGenerator extends AbstractTerrainGenerator {

    public MustafarTerrainGenerator(HexVGPlanets plugin, WorldInfo worldInfo) {
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

        placeMustafarBedrock(chunk, x, z, rng);


        double dLavaLevel = getLavaLevel(rx, rz);
        int lavaLevel = (int) dLavaLevel;


        fill(chunk, x, z, minY + 6, minY + 9, Material.LAVA);


        for (int y = minY + 10; y <= minY + 15; y++) {
            double h = noise.noise(rx, rz, 8, 11) * 6.0;
            chunk.setBlock(x, y, z, (y - (minY + 10)) > h ? Material.STONE : Material.LAVA);
        }


        int bulkTop = seaY + surfaceOffset - 20;
        if (bulkTop < minY + 16) bulkTop = minY + 16;
        fill(chunk, x, z, minY + 16, bulkTop, Material.STONE);
        int y = bulkTop + 1;


        double ml1 = noise.noise(rx, rz, 2, 236) * 16.0;
        double ml2 = noise.noise(rx, rz, 2, 33)  * 8.0;
        double ml3 = noise.noise(rx, rz, 2, 22)  * 12.0;
        double ml4 = noise.noise(rx, rz, 2, 7)   * 16.0 * noise.noise(rx, rz, 2, 125);
        double fn1 = noise.noise(rx, rz, 2, 235);
        double fn2 = fn1 * noise.noise(rx, rz, 2, 3) * 4.0;
        int ml = (int)(ml1 + ml2 + ml3 + ml4 + fn2);
        fill(chunk, x, z, y, y + ml, Material.STONE);
        y += ml;


        double doMtn = noise.noise(rx, rz, 4, 635) * 20.0;
        if (doMtn > 10.0) {
            double mf = (doMtn - 10.0) / 10.0;
            double mh = noise.noise(rx, rz, 4, 87) * 104.0
                    + noise.noise(rx, rz, 8, 50) * 49.0
                    + noise.noise(rx, rz, 8, 4)  * 7.0;
            int mhI = (int)(mh * mf);
            int mb  = minY + 26 + surfaceOffset;
            for (int i = 0; i < mhI; i++) {
                int my = mb + i;
                if (my < maxY) { chunk.setBlock(x, my, z, Material.STONE); if (my > y) y = my; }
            }
        }


        double r1 = noise.noise(rx, rz, 2, 350) * 96.0 - 16.0;
        double r2 = noise.noise(rx, rz, 2, 830) * 96.0 - 16.0;
        double r  = r1 + r2;
        if (r > 60 && r < 76) {
            double height = (3 - Math.abs(68 - r) * Math.abs(68 - r)) / 1.5;
            int rivY = lavaLevel - (int) height;
            while (rivY < maxY && chunk.getType(x, rivY, z) != Material.AIR) {
                chunk.setBlock(x, rivY, z, Material.AIR);
                rivY++;
            }
        }


        int wy = lavaLevel;
        while (wy > minY && (chunk.getType(x, wy, z) == Material.AIR || chunk.getType(x, wy, z) == Material.LAVA)) {
            chunk.setBlock(x, wy, z, Material.LAVA);
            wy--;
        }


        double tubeA = noise.noise(rx, rz, 2, 150) * 4.0 + noise.noise(rx, rz, 2, 43) * 4.0;
        for (int i = minY + 10; i < minY + 144 + surfaceOffset; i++) {
            double d = noise.noise(rx, i, rz, 4, 10) * 16.0;
            double a = tubeA;
            if (i > seaY + surfaceOffset + 32) a += 8.0 * ((i - (seaY + surfaceOffset + 32.0)) / 32.0);
            if (d > a + 8 && chunk.getType(x, i, z) == Material.STONE) {
                chunk.setBlock(x, i, z, Material.LAVA);
            }
        }


        double caveA = noise.noise(rx, rz, 2, 160) * 4.0 + noise.noise(rx, rz, 2, 42) * 4.0;
        for (int i = minY + 10; i < minY + 144 + surfaceOffset; i++) {
            double d = noise.noise(rx, i, rz, 4, 11) * 16.0;
            double a = caveA;
            if (i > seaY + surfaceOffset + 32) a += 8.0 * ((i - (seaY + surfaceOffset + 32.0)) / 32.0);
            if (d > a + 8 && chunk.getType(x, i, z) == Material.STONE) {
                chunk.setBlock(x, i, z, Material.AIR);
            }
        }
    }


    private double getLavaLevel(int rx, int rz) {
        double base = seaY + surfaceOffset;
        double n1   = noise.noise(rx, rz, 2, 130) * 10.0 - 5.0;
        double n2   = noise.noise(rx, rz, 2, 77)  * 6.0  - 3.0;
        return base + n1 + n2;
    }

    private void queueStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        StructureQueue.queue(worldInfo, random, chunkX, chunkZ,
                com.venomgrave.hexvg.world.PlanetType.MUSTAFAR);
    }
}