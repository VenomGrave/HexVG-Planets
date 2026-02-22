package com.venomgrave.hexvg.loot;

import org.bukkit.Material;

public class LootEntry {
    private final Material material;
    private final int weight;
    private final int min;
    private final int max;

    public LootEntry(Material material, int weight, int min, int max) {
        this.material = material;
        this.weight   = weight;
        this.min      = min;
        this.max      = max;
    }

    public Material getMaterial() { return material; }
    public int getWeight()        { return weight; }
    public int getMin()           { return min; }
    public int getMax()           { return max; }
}