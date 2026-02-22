package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class PlanetUtils {

    private PlanetUtils() {}


    public static boolean isTooHot(Location location, int radius) {
        World world = location.getWorld();
        if (world == null) return false;
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        for (int xx = x - radius; xx <= x + radius; xx++)
            for (int zz = z - radius; zz <= z + radius; zz++)
                for (int yy = y - radius; yy <= y + radius; yy++)
                    if (world.getBlockAt(xx, yy, zz).getType() == Material.LAVA) return true;
        return false;
    }


    public static boolean canPlaceLiquid(HexVGPlanets plugin, World world, Block targetBlock) {
        // TODO: opcjonalna integracja WorldGuard
        return false;
    }

    /**
     * Sprawdza czy blok zawiera wodę (w tym wodorosty, bubble column).
     */
    public static boolean isWater(Block block) {
        switch (block.getType()) {
            case WATER:
            case BUBBLE_COLUMN:
            case KELP:
            case KELP_PLANT:
            case SEAGRASS:
            case TALL_SEAGRASS:
                return true;
            default:
                return false;
        }
    }
}