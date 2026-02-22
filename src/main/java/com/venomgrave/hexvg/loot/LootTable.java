package com.venomgrave.hexvg.loot;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootTable {

    private final String name;
    private final List<LootEntry> entries;
    private final int minItems;
    private final int maxItems;
    private final int totalWeight;

    public LootTable(String name, List<LootEntry> entries, int minItems, int maxItems) {
        this.name     = name;
        this.entries  = entries;
        this.minItems = minItems;
        this.maxItems = maxItems;
        int w = 0;
        for (LootEntry e : entries) w += e.getWeight();
        this.totalWeight = w;
    }

    public static LootTable load(FileConfiguration cfg) {
        String name = cfg.getString("name", "unnamed");
        int min = cfg.getInt("minItems", 3);
        int max = cfg.getInt("maxItems", 10);
        List<LootEntry> entries = new ArrayList<>();
        for (var section : cfg.getConfigurationSection("entries").getKeys(false)) {
            String path = "entries." + section;
            entries.add(new LootEntry(
                    org.bukkit.Material.valueOf(cfg.getString(path + ".material").toUpperCase()),
                    cfg.getInt(path + ".weight", 1),
                    cfg.getInt(path + ".min", 1),
                    cfg.getInt(path + ".max", 1)
            ));
        }
        return new LootTable(name, entries, min, max);
    }

    public LootEntry roll(Random random) {
        if (entries.isEmpty() || totalWeight == 0) return null;
        int roll = random.nextInt(totalWeight);
        int acc  = 0;
        for (LootEntry e : entries) {
            acc += e.getWeight();
            if (roll < acc) return e;
        }
        return entries.get(entries.size() - 1);
    }

    public String getName()         { return name; }
    public List<LootEntry> getEntries() { return entries; }
    public int getMinItems()        { return minItems; }
    public int getMaxItems()        { return maxItems; }
}