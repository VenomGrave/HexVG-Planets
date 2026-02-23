package com.venomgrave.hexvg.loot;

import org.bukkit.Material;
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

        var sec = cfg.getConfigurationSection("entries");
        if (sec == null) {
            System.out.println("[Loot] Loot table '" + name + "' has no entries section.");
            return new LootTable(name, entries, min, max);
        }

        for (String key : sec.getKeys(false)) {
            String path = "entries." + key;

            String matName = cfg.getString(path + ".material", "").toUpperCase();
            Material mat = Material.matchMaterial(matName);

            if (mat == null) {
                System.out.println("[Loot] Invalid material '" + matName + "' in loot table '" + name + "'");
                continue;
            }

            int weight = cfg.getInt(path + ".weight", 1);
            int minQty = cfg.getInt(path + ".min", 1);
            int maxQty = cfg.getInt(path + ".max", 1);

            if (weight <= 0) {
                System.out.println("[Loot] Invalid weight for '" + matName + "' in loot table '" + name + "'");
                continue;
            }

            entries.add(new LootEntry(mat, weight, minQty, maxQty));
        }

        return new LootTable(name, entries, min, max);
    }

    public LootEntry roll(Random random) {
        if (entries.isEmpty() || totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight);
        int acc  = 0;

        for (LootEntry e : entries) {
            acc += e.getWeight();
            if (roll < acc) return e;
        }

        return entries.get(entries.size() - 1);
    }

    public String getName() { return name; }
    public List<LootEntry> getEntries() { return entries; }
    public int getMinItems() { return minItems; }
    public int getMaxItems() { return maxItems; }
}
