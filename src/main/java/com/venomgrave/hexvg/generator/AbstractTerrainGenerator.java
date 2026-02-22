// src/main/java/com/venomgrave/hexvg/generator/AbstractTerrainGenerator.java
package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public abstract class AbstractTerrainGenerator implements TerrainGenerator {

    protected final HexVGPlanets plugin;
    protected final WorldInfo worldInfo;
    protected final NoiseGenerator noise;
    protected final int surfaceOffset;

    protected final int minY;
    protected final int maxY;

    protected final int seaY;

    protected AbstractTerrainGenerator(HexVGPlanets plugin, WorldInfo worldInfo) {
        this.plugin = plugin;
        this.worldInfo = worldInfo;
        this.noise = new NoiseGenerator(worldInfo.getSeed());
        this.surfaceOffset = ConfigManager.getWorldSurfaceOffset(plugin, worldInfo.getName());

        this.minY = worldInfo.getMinHeight();
        this.maxY = worldInfo.getMaxHeight();

        this.seaY = 63;
    }


    protected void placeBedrock(ChunkData chunk, int x, int z, Random rng) {
        int y0 = clampY(chunk, minY);
        setIfInRange(chunk, x, y0, z, Material.BEDROCK);
        setIfInRange(chunk, x, y0 + 1, z, randomBedrock(rng, 0.90));
        setIfInRange(chunk, x, y0 + 2, z, randomBedrock(rng, 0.70));
        setIfInRange(chunk, x, y0 + 3, z, randomBedrock(rng, 0.50));
        setIfInRange(chunk, x, y0 + 4, z, randomBedrock(rng, 0.30));
        setIfInRange(chunk, x, y0 + 5, z, randomBedrock(rng, 0.20));
    }

    protected void placeMustafarBedrock(ChunkData chunk, int x, int z, Random rng) {
        int y0 = clampY(chunk, minY);
        setIfInRange(chunk, x, y0, z, Material.BEDROCK);
        setIfInRange(chunk, x, y0 + 1, z, randomBedrockOrLava(rng, 0.90));
        setIfInRange(chunk, x, y0 + 2, z, randomBedrockOrLava(rng, 0.70));
        setIfInRange(chunk, x, y0 + 3, z, randomBedrockOrLava(rng, 0.50));
        setIfInRange(chunk, x, y0 + 4, z, randomBedrockOrLava(rng, 0.30));
        setIfInRange(chunk, x, y0 + 5, z, randomBedrockOrLava(rng, 0.20));
    }

    private Material randomBedrock(Random rng, double bedrockChance) {
        return rng.nextDouble() < bedrockChance ? Material.BEDROCK : Material.STONE;
    }

    private Material randomBedrockOrLava(Random rng, double bedrockChance) {
        return rng.nextDouble() < bedrockChance ? Material.BEDROCK : Material.LAVA;
    }

    protected int toAbsY(int legacyY) {
        return legacyY + minY;
    }

    protected int toAbsYScaled(int legacyY) {
        int clamped = clamp(legacyY, 0, 255);
        int top = maxY - 1;
        int range = top - minY; // inclusive range length in blocks

        return minY + (clamped * range) / 255;
    }

    protected int getSeaY() {
        return seaY;
    }

    protected void fillStone(ChunkData chunk, int x, int z, int fromY, int toY) {
        fill(chunk, x, z, fromY, toY, Material.STONE);
    }

    protected void fill(ChunkData chunk, int x, int z, int fromY, int toY, Material mat) {
        if (mat == null) return;

        int yMin = Math.max(chunkMinY(chunk), minY);
        int yMaxIncl = Math.min(chunkMaxYExclusive(chunk) - 1, maxY - 1);

        int a = clamp(fromY, yMin, yMaxIncl);
        int b = clamp(toY, yMin, yMaxIncl);
        if (a > b) return;

        for (int y = a; y <= b; y++) {
            chunk.setBlock(x, y, z, mat);
        }
    }


    private void setIfInRange(ChunkData chunk, int x, int y, int z, Material mat) {
        int yMin = Math.max(chunkMinY(chunk), minY);
        int yMaxEx = Math.min(chunkMaxYExclusive(chunk), maxY);
        if (y >= yMin && y < yMaxEx) {
            chunk.setBlock(x, y, z, mat);
        }
    }

    private int clampY(ChunkData chunk, int y) {
        int yMin = Math.max(chunkMinY(chunk), minY);
        int yMaxIncl = Math.min(chunkMaxYExclusive(chunk) - 1, maxY - 1);
        return clamp(y, yMin, yMaxIncl);
    }

    private int chunkMinY(ChunkData chunk) {
        // Available in 1.18+ API
        return chunk.getMinHeight();
    }

    private int chunkMaxYExclusive(ChunkData chunk) {
        // Available in 1.18+ API
        return chunk.getMaxHeight();
    }

    private int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}