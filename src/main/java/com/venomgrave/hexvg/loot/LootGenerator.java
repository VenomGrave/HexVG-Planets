package com.venomgrave.hexvg.loot;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.logging.Level;


public class LootGenerator {

    private final HexVGPlanets plugin;
    private final Map<String, LootTable> tables = new LinkedHashMap<>();
    private LootTable defaultTable;
    private final Random random = new Random();

    public LootGenerator(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    public void load() {
        tables.clear();

        // Load from data folder
        File dir = new File(plugin.getDataFolder(), "loot");
        if (dir.exists()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".yml"));
            if (files != null) {
                for (File f : files) {
                    try {
                        LootTable t = LootTable.load(YamlConfiguration.loadConfiguration(f));
                        tables.put(t.getName(), t);
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to load loot table: " + f.getName(), e);
                    }
                }
            }
        }

        buildDefaultTable();
    }

    public void fill(Inventory inv) {
        fill(inv, defaultTable,
                defaultTable.getMinItems(),
                defaultTable.getMaxItems());
    }

    public void fill(Inventory inv, String tableName, int minItems, int maxItems) {
        LootTable t = tables.getOrDefault(tableName, defaultTable);
        fill(inv, t, minItems, maxItems);
    }

    private void fill(Inventory inv, LootTable table, int min, int max) {
        if (table == null || table.getEntries().isEmpty()) return;

        int count = min + random.nextInt(Math.max(1, max - min + 1));
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inv.getSize(); i++) slots.add(i);
        Collections.shuffle(slots, random);

        for (int i = 0; i < Math.min(count, slots.size()); i++) {
            LootEntry entry = table.roll(random);
            if (entry == null) continue;
            int qty = entry.getMin() + random.nextInt(Math.max(1, entry.getMax() - entry.getMin() + 1));
            inv.setItem(slots.get(i), new ItemStack(entry.getMaterial(), qty));
        }
    }

    private void buildDefaultTable() {
        List<LootEntry> entries = new ArrayList<>(Arrays.asList(
                new LootEntry(Material.IRON_INGOT,     15, 2, 8),
                new LootEntry(Material.GOLD_INGOT,      5, 1, 3),
                new LootEntry(Material.DIAMOND,          1, 1, 1),
                new LootEntry(Material.BREAD,           10, 1, 6),
                new LootEntry(Material.TORCH,           10, 4, 16),
                new LootEntry(Material.OAK_PLANKS,       8, 4, 12),
                new LootEntry(Material.STONE_SWORD,      3, 1, 1),
                new LootEntry(Material.IRON_SWORD,       2, 1, 1),
                new LootEntry(Material.LEATHER_HELMET,   3, 1, 1),
                new LootEntry(Material.IRON_HELMET,      2, 1, 1),
                new LootEntry(Material.BOW,              3, 1, 1),
                new LootEntry(Material.ARROW,            8, 4, 16)
        ));
        defaultTable = new LootTable("default", entries, 3, 10);
    }
}