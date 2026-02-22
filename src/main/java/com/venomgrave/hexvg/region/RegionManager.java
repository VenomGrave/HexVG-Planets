package com.venomgrave.hexvg.region;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class RegionManager {

    private final HexVGPlanets plugin;
    private final File file;
    private YamlConfiguration yaml;
    private final List<PlanetRegion> regions = new ArrayList<>();

    public RegionManager(HexVGPlanets plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "regions.yml");
    }


    public void load() {
        regions.clear();
        if (!file.exists()) { save(); return; }
        yaml = YamlConfiguration.loadConfiguration(file);
        if (!yaml.isConfigurationSection("regions")) return;

        for (String key : yaml.getConfigurationSection("regions").getKeys(false)) {
            String p = "regions." + key;
            PlanetRegion r = new PlanetRegion(
                    key,
                    yaml.getString(p + ".world", ""),
                    yaml.getInt(p + ".minX"), yaml.getInt(p + ".minY"), yaml.getInt(p + ".minZ"),
                    yaml.getInt(p + ".maxX"), yaml.getInt(p + ".maxY"), yaml.getInt(p + ".maxZ")
            );
            if (yaml.isConfigurationSection(p + ".flags")) {
                for (String fk : yaml.getConfigurationSection(p + ".flags").getKeys(false)) {
                    r.setFlag(fk, yaml.getString(p + ".flags." + fk, "true"));
                }
            }
            regions.add(r);
        }
    }

    public void save() {
        yaml = new YamlConfiguration();
        for (PlanetRegion r : regions) {
            String p = "regions." + r.getName();
            yaml.set(p + ".world", r.getWorld());
            yaml.set(p + ".minX", r.getMinX()); yaml.set(p + ".minY", r.getMinY()); yaml.set(p + ".minZ", r.getMinZ());
            yaml.set(p + ".maxX", r.getMaxX()); yaml.set(p + ".maxY", r.getMaxY()); yaml.set(p + ".maxZ", r.getMaxZ());
            r.getFlags().forEach((k, v) -> yaml.set(p + ".flags." + k, v));
        }
        try { yaml.save(file); } catch (IOException e) {
            plugin.getLogger().severe("Failed to save regions.yml: " + e.getMessage());
        }
    }


    public boolean hasFlag(Location loc, String flag) {
        if (loc.getWorld() == null) return false;
        String worldName = loc.getWorld().getName();
        for (PlanetRegion r : regions) {
            if (!r.getWorld().equalsIgnoreCase(worldName)) continue;
            if (!r.contains(loc)) continue;
            if (r.getFlags().containsKey(flag)) return true;
        }
        return false;
    }

    public Optional<String> getFlagValue(Location loc, String flag) {
        if (loc.getWorld() == null) return Optional.empty();
        String worldName = loc.getWorld().getName();
        for (PlanetRegion r : regions) {
            if (!r.getWorld().equalsIgnoreCase(worldName)) continue;
            if (!r.contains(loc)) continue;
            String val = r.getFlags().get(flag);
            if (val != null) return Optional.of(val);
        }
        return Optional.empty();
    }

    public void addRegion(PlanetRegion region) {
        regions.removeIf(r -> r.getName().equalsIgnoreCase(region.getName()));
        regions.add(region);
        save();
    }

    public boolean removeRegion(String name) {
        boolean removed = regions.removeIf(r -> r.getName().equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    public Optional<PlanetRegion> getRegion(String name) {
        return regions.stream().filter(r -> r.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<PlanetRegion> getRegionsAt(Location loc) {
        List<PlanetRegion> found = new ArrayList<>();
        if (loc.getWorld() == null) return found;
        String worldName = loc.getWorld().getName();
        for (PlanetRegion r : regions) {
            if (r.getWorld().equalsIgnoreCase(worldName) && r.contains(loc)) found.add(r);
        }
        return found;
    }

    public List<PlanetRegion> getAll() {
        return Collections.unmodifiableList(regions);
    }

    public List<PlanetRegion> getAllRegions() {
        return Collections.unmodifiableList(regions);
    }
}