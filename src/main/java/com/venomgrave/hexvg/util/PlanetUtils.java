package com.venomgrave.hexvg.util;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public final class PlanetUtils {
    private PlanetUtils() {}

    private static final BlockFace[] ADJACENTS = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.DOWN,  BlockFace.UP
    };

    public static boolean isTooHot(Location loc, int radius) {
        World world = loc.getWorld();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Material m = world.getBlockAt(
                            loc.getBlockX() + dx,
                            loc.getBlockY() + dy,
                            loc.getBlockZ() + dz
                    ).getType();
                    if (m == Material.LAVA || m == Material.FIRE) return true;
                }
            }
        }
        return false;
    }

    public static boolean canPlaceLiquid(HexVGPlanets plugin, World world, Block block) {
        return plugin.getConfigManager().getBooleanForWorld(world.getName(), "rules.placewater", true);
    }

    public static boolean hasAdjacentSolid(Block block) {
        for (BlockFace face : ADJACENTS) {
            Block adj = block.getRelative(face);
            if (!adj.getType().isAir() && !adj.isLiquid()) return true;
        }
        return false;
    }

    public static boolean isOutdoors(Location loc) {
        World world = loc.getWorld();
        for (int y = loc.getBlockY() + 1; y < world.getMaxHeight(); y++) {
            if (!world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isInOrNearWater(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (world.getBlockAt(x + dx, y, z + dz).getType() == Material.WATER ||
                        world.getBlockAt(x + dx, y, z + dz).getType() == Material.WATER) {
                    return true;
                }
            }
        }
        return world.getBlockAt(x, y - 1, z).getType() == Material.WATER;
    }
}