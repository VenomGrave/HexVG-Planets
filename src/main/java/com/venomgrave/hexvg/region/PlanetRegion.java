package com.venomgrave.hexvg.region;

import org.bukkit.Location;

import java.util.LinkedHashMap;
import java.util.Map;


public class PlanetRegion {

    private final String name;
    private final String world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final Map<String, String> flags = new LinkedHashMap<>();

    public PlanetRegion(String name, String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.name  = name;
        this.world = world;
        this.minX  = Math.min(minX, maxX); this.maxX = Math.max(minX, maxX);
        this.minY  = Math.min(minY, maxY); this.maxY = Math.max(minY, maxY);
        this.minZ  = Math.min(minZ, maxZ); this.maxZ = Math.max(minZ, maxZ);
    }

    public boolean contains(Location loc) {
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public void setFlag(String key, String value) { flags.put(key, value); }
    public void removeFlag(String key)            { flags.remove(key); }

    public String getName()  { return name; }
    public String getWorld() { return world; }
    public int getMinX()     { return minX; }
    public int getMinY()     { return minY; }
    public int getMinZ()     { return minZ; }
    public int getMaxX()     { return maxX; }
    public int getMaxY()     { return maxY; }
    public int getMaxZ()     { return maxZ; }
    public Map<String, String> getFlags() { return flags; }

    @Override
    public String toString() {
        return name + " [" + world + " (" + minX + "," + minY + "," + minZ + ") → (" + maxX + "," + maxY + "," + maxZ + ")]";
    }
}