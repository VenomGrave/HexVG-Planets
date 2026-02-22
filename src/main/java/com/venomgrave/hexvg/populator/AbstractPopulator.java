package com.venomgrave.hexvg.populator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;


public abstract class AbstractPopulator extends BlockPopulator {


    protected static final int WORLD_MIN_Y  = -64;
    protected static final int WORLD_MAX_Y  = 320;
    protected static final int SCAN_TOP_Y   = 250;
    protected static final int SEA_Y        = 63;


    protected static int findSurfaceY(int x, int z, LimitedRegion region) {
        for (int y = SCAN_TOP_Y; y > WORLD_MIN_Y; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            if (region.getType(x, y, z) != Material.AIR) return y + 1;
        }
        return WORLD_MIN_Y + 1;
    }

    protected static int findSolidY(int x, int z, LimitedRegion region) {
        for (int y = SCAN_TOP_Y; y > WORLD_MIN_Y; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            Material m = region.getType(x, y, z);
            if (!m.isAir() && m != Material.WATER) return y + 1;
        }
        return WORLD_MIN_Y + 1;
    }

    protected static int findSandSurfaceY(int x, int z, LimitedRegion region) {
        for (int y = SCAN_TOP_Y; y > WORLD_MIN_Y; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            Material m = region.getType(x, y, z);
            if (!m.isAir()) return y + 1;
        }
        return WORLD_MIN_Y + 1;
    }

    protected static int findLavaTopY(int x, int z, LimitedRegion region) {
        for (int y = SCAN_TOP_Y; y > WORLD_MIN_Y; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            Material m = region.getType(x, y, z);
            if (m == Material.LAVA) return y + 1;
        }
        return findSolidY(x, z, region);
    }

    protected static void placeOreVein(LimitedRegion region, Material mat, int cx, int cy, int cz, int size, Random rng, Material... hosts) {
        for (int i = 0; i < size; i++) {
            int bx = cx + rng.nextInt(3) - 1;
            int by = cy + rng.nextInt(3) - 1;
            int bz = cz + rng.nextInt(3) - 1;
            if (!region.isInRegion(bx, by, bz)) continue;
            Material existing = region.getType(bx, by, bz);
            for (Material h : hosts) {
                if (existing == h) {
                    region.setType(bx, by, bz, mat);
                    break;
                }
            }
        }
    }

    protected static boolean tryPlaceSchematic(LimitedRegion region, WorldInfo worldInfo, int chunkX, int chunkZ, String schematicName, int rarity, Random rng, int surfaceY) {
        if (rarity <= 0 || rng.nextInt(rarity) != 0) return false;

        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return false;

        Optional<Schematic> opt = plugin.getSchematicRegistry().get(schematicName);
        if (!opt.isPresent()) return false;

        Schematic rotated = SchematicRotator.rotate(opt.get(), rng.nextInt(4));
        int ox = chunkX * 16 + rng.nextInt(16);
        int oz = chunkZ * 16 + rng.nextInt(16);

        SchematicPlacer.placeInRegion(region, rotated, ox, surfaceY, oz,
                plugin.getLootGenerator());
        return true;
    }

    protected static boolean tryPlaceSchematic(LimitedRegion region, WorldInfo worldInfo, int chunkX, int chunkZ, String schematicName, int rarity, Random rng) {
        if (rarity <= 0 || rng.nextInt(rarity) != 0) return false;

        int sx = chunkX * 16 + rng.nextInt(16);
        int sz = chunkZ * 16 + rng.nextInt(16);
        int sy = findSurfaceY(sx, sz, region);

        return tryPlaceSchematic(region, worldInfo, chunkX, chunkZ, schematicName, 1, rng, sy);
    }

    protected static int getStructureRarity(String worldName, String structureKey, int defaultRarity) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return defaultRarity;
        return plugin.getConfigManager().getIntForWorld(
                worldName, "structure." + structureKey + ".rarity", defaultRarity);
    }

    protected static boolean isSafe(LimitedRegion region, int x, int y, int z) {
        return y > WORLD_MIN_Y && y < WORLD_MAX_Y - 1 && region.isInRegion(x, y, z);
    }

    protected static boolean hasVerticalClearance(LimitedRegion region, int x, int y, int z, int minHeight) {
        for (int dy = 0; dy < minHeight; dy++) {
            if (!region.isInRegion(x, y + dy, z)) return false;
            if (!region.getType(x, y + dy, z).isAir()) return false;
        }
        return true;
    }
}