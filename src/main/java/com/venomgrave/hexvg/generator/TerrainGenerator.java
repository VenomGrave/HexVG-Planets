package com.venomgrave.hexvg.generator;

import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;


public interface TerrainGenerator {


    void generate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk);
}