package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.generator.StructureQueue;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;


public class HothTerrainGenerator extends AbstractTerrainGenerator {

    public HothTerrainGenerator(HexVGPlanets plugin, WorldInfo worldInfo) {
        super(plugin, worldInfo);
    }

    @Override
    public void generate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        Random localRng = new Random((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L + worldInfo.getSeed());

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int rx = chunkX * 16 + x;
                int rz = chunkZ * 16 + z;

                generateColumn(chunk, localRng, rx, rz, x, z);
            }
        }

        queueStructures(worldInfo, random, chunkX, chunkZ);
    }


    private void generateColumn(ChunkData chunk, Random rng, int rx, int rz, int x, int z) {


        placeBedrock(chunk, x, z, rng);

        int y = minY + 6;


        int solidStoneTop = minY + 6 + 21 + surfaceOffset;
        fill(chunk, x, z, y, solidStoneTop, Material.STONE);
        y = solidStoneTop + 1;


        int stoneExtra = (int)(noise.noise(rx, rz, 8, 16) * 3.0);
        fill(chunk, x, z, y, y + stoneExtra, Material.STONE);
        y += stoneExtra;


        int dirtH = (int)(noise.noise(rx, rz, 8, 11) * 5.0);
        if (dirtH > 2) {
            fill(chunk, x, z, y, y + (dirtH - 2), Material.DIRT);
            y += (dirtH - 2);
        }


        int gravelH = (int)(noise.noise(rx, rz, 7, 16) * 5.0);
        if (gravelH > 2) {
            fill(chunk, x, z, y, y + (gravelH - 2), Material.GRAVEL);
            y += (gravelH - 2);
        }


        int sandstoneH = (int)(noise.noise(rx, rz, 8, 23) * 4.0);
        if (sandstoneH > 1) {
            fill(chunk, x, z, y, y + (sandstoneH - 1), Material.SANDSTONE);
            y += (sandstoneH - 1);
        }


        int sandH = 1 + (int)(noise.noise(rx, rz, 8, 43) * 4.0);
        fill(chunk, x, z, y, y + sandH, Material.SAND);
        y += sandH;


        int clayH = 1 + (int)(noise.noise(rx, rz, 3, 9) * 5.0);
        if (clayH > 3) {
            chunk.setBlock(x, y, z, Material.TERRACOTTA);
            y++;
            fill(chunk, x, z, y, y + (clayH - 4), Material.CLAY);
            y += (clayH - 4);
        }


        int iceFloor = minY + 34 + surfaceOffset;
        while (y < iceFloor) {
            chunk.setBlock(x, y, z, Material.ICE);
            y++;
        }


        int iceExtra = (int)(noise.noise(rx, rz, 3, 68) * 8.0);
        if (iceExtra > 3) {
            fill(chunk, x, z, y, y + (iceExtra - 3), Material.ICE);
            y += (iceExtra - 3);
        }


        double iceMountain = noise.noise(rx, rz, 3, 7) * 15.0;
        double iceHeight   = noise.noise(rx, rz, 4, 63) * 2.0 + noise.noise(rx, rz, 10, 12);
        double ice         = iceHeight * 2.5;

        int iceTop   = seaY + surfaceOffset + (int) ice;
        double diceTop = seaY + surfaceOffset + ice;


        int packedTop = iceTop - (int) iceMountain;
        if (packedTop > y) {
            fill(chunk, x, z, y, packedTop, Material.PACKED_ICE);
            y = packedTop + 1;
        }

        if (iceTop > y) {
            fill(chunk, x, z, y, iceTop, Material.ICE);
            y = iceTop + 1;
        }


        double doMountain = noise.noise(rx, rz, 4, 236) * 20.0;
        if (doMountain > 16.0) {
            double mFactor = doMountain > 18.0 ? 1.0 : (doMountain - 16.0) * 0.5;
            double mountain = noise.noise(rx, rz, 4, 27) * 84.0
                    + noise.noise(rx, rz, 8, 3) * 5.0;
            int mHeight = (int)(mountain * mFactor);
            int mBase   = minY + 26 + surfaceOffset;
            for (int i = 0; i < mHeight; i++) {
                int my = mBase + i;
                if (my < maxY) {
                    chunk.setBlock(x, my, z, Material.STONE);
                    if (my > y) y = my;
                }
            }
        }


        int snowBlocks = 1 + (int)(noise.noise(rx, rz, 8, 76) * 2.0);
        double subSnow  = diceTop - (int) diceTop;
        int snowCount   = (int)(snowBlocks + subSnow);
        fill(chunk, x, z, y, y + snowCount, Material.SNOW_BLOCK);
        y += snowCount;


        if (y < maxY) {
            chunk.setBlock(x, y, z, Material.SNOW);
        }


        double doLava = noise.noise(rx, rz, 4, 71) * 10.0;
        if (doLava > 7.0) {
            double lava  = noise.noise(rx, rz, 4, 7) * 21.0;
            int lavaDep  = (int) lava - 18;
            if (lavaDep > -2) {
                int start = minY + 8 - (2 + lavaDep) / 2;
                for (int i = -1; i < lavaDep; i++) {
                    int ly = start + i;
                    if (ly < minY + 7)      chunk.setBlock(x, ly, z, Material.LAVA);
                    else if (ly < maxY)     chunk.setBlock(x, ly, z, Material.AIR);
                }
            }
        }


        double doWater = noise.noise(rx, rz, 4, 91) * 10.0;
        if (doWater > 7.0) {
            double water  = noise.noise(rx, rz, 4, 8) * 21.0;
            int waterDep  = (int) water - 18;
            if (waterDep > -2) {
                int start = minY + 23 - (2 + waterDep) / 2;
                for (int i = -1; i < waterDep; i++) {
                    int wy = start + i;
                    if (wy < minY + 22)     chunk.setBlock(x, wy, z, Material.WATER);
                    else if (wy < maxY)     chunk.setBlock(x, wy, z, Material.AIR);
                }
            }
        }
    }


    private void queueStructures(WorldInfo worldInfo, Random random, int chunkX, int chunkZ) {
        StructureQueue.queue(worldInfo, random, chunkX, chunkZ,
                com.venomgrave.hexvg.world.PlanetType.HOTH);
    }
}