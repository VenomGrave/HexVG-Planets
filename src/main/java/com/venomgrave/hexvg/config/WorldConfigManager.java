package com.venomgrave.hexvg.config;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class WorldConfigManager {

    private final HexVGPlanets plugin;
    private final File configFile;
    private YamlConfiguration yaml;

    // In-memory cache
    private final Map<String, PlanetType>           typeMap  = new LinkedHashMap<>();
    private final Map<String, Map<String, String>>  flagMap  = new LinkedHashMap<>();

    public WorldConfigManager(HexVGPlanets plugin) {
        this.plugin     = plugin;
        this.configFile = new File(plugin.getDataFolder(), "worldConfig.yml");
        load();
    }


    public void load() {
        typeMap.clear();
        flagMap.clear();

        if (!configFile.exists()) {
            save(); // create empty file
            return;
        }

        yaml = YamlConfiguration.loadConfiguration(configFile);

        if (!yaml.isConfigurationSection("worlds")) return;

        for (String worldName : yaml.getConfigurationSection("worlds").getKeys(false)) {
            String typeName = yaml.getString("worlds." + worldName + ".type", "hoth");
            typeMap.put(worldName.toLowerCase(), PlanetType.fromString(typeName));

            Map<String, String> flags = new LinkedHashMap<>();
            if (yaml.isConfigurationSection("worlds." + worldName + ".flags")) {
                for (String key : yaml.getConfigurationSection("worlds." + worldName + ".flags").getKeys(false)) {
                    flags.put(key, yaml.getString("worlds." + worldName + ".flags." + key, ""));
                }
            }
            flagMap.put(worldName.toLowerCase(), flags);
        }
    }

    public void save() {
        yaml = new YamlConfiguration();
        for (Map.Entry<String, PlanetType> entry : typeMap.entrySet()) {
            String wn = entry.getKey();
            yaml.set("worlds." + wn + ".type", entry.getValue().toKey());
            Map<String, String> flags = flagMap.getOrDefault(wn, Collections.emptyMap());
            for (Map.Entry<String, String> fe : flags.entrySet()) {
                yaml.set("worlds." + wn + ".flags." + fe.getKey(), fe.getValue());
            }
        }
        try {
            yaml.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save worldConfig.yml: " + e.getMessage());
        }
    }


    public void registerWorld(String worldName, PlanetType type) {
        String key = worldName.toLowerCase();
        typeMap.put(key, type);
        flagMap.putIfAbsent(key, new LinkedHashMap<>());
        save();
    }

    public void unregisterWorld(String worldName) {
        String key = worldName.toLowerCase();
        typeMap.remove(key);
        flagMap.remove(key);
        save();
    }

    public boolean isRegistered(String worldName) {
        return typeMap.containsKey(worldName.toLowerCase());
    }

    public PlanetType getType(String worldName) {
        return typeMap.getOrDefault(worldName.toLowerCase(), PlanetType.HOTH);
    }

    public boolean setType(String worldName, PlanetType type) {
        if (!isRegistered(worldName)) return false;
        typeMap.put(worldName.toLowerCase(), type);
        save();
        return true;
    }


    public String getFlag(String worldName, String flag) {
        Map<String, String> flags = flagMap.get(worldName.toLowerCase());
        if (flags == null) return null;
        return flags.get(flag);
    }


    public boolean setFlag(String worldName, String flag, String value) {
        if (!isRegistered(worldName)) return false;
        Map<String, String> flags = flagMap.get(worldName.toLowerCase());
        if (value == null || value.isEmpty()) {
            flags.remove(flag);
        } else {
            flags.put(flag, value);
        }
        save();
        return true;
    }


    public Set<String> getRegisteredWorlds() {
        return Collections.unmodifiableSet(typeMap.keySet());
    }

    public String getWorldInfo(String worldName) {
        if (!isRegistered(worldName)) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("type=").append(typeMap.get(worldName.toLowerCase()).toKey());
        Map<String, String> flags = flagMap.get(worldName.toLowerCase());
        if (flags != null && !flags.isEmpty()) {
            sb.append(", flags=").append(flags);
        }
        return sb.toString();
    }
}