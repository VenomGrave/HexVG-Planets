package com.venomgrave.hexvg.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;


public final class WorldUtil {
    private WorldUtil() {}

    public static int minY(World world) {
        return world.getMinHeight();
    }

    public static int maxY(World world) {
        return world.getMaxHeight();
    }

    public static int seaY(World world) {
        return world.getSeaLevel();
    }

    public static int getSurfaceY(World world, int x, int z) {
        int max = world.getMaxHeight() - 1;
        int min = world.getMinHeight();
        for (int y = max; y >= min; y--) {
            Block b = world.getBlockAt(x, y, z);
            if (!b.getType().isAir()) return y;
        }
        return min;
    }

    public static int playerFeetY(Location loc) {
        return loc.getBlockY();
    }
}