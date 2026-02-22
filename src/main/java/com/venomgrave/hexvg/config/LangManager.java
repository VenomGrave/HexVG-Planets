package com.venomgrave.hexvg.config;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class LangManager {

    private final HexVGPlanets plugin;
    private final Logger log;
    private YamlConfiguration lang;
    private YamlConfiguration fallback;

    public LangManager(HexVGPlanets plugin) {
        this.plugin = plugin;
        this.log    = plugin.getLogger();
        reload();
    }


    public String get(String key) {
        String raw = null;

        if (lang != null && lang.contains(key)) {
            raw = lang.getString(key);
        } else if (fallback != null && fallback.contains(key)) {
            raw = fallback.getString(key);
        }

        if (raw == null) {
            log.warning("[LangManager] Brakujący klucz: " + key);
            return "[?" + key + "]";
        }

        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String get(String key, Object... args) {
        String msg = get(key);
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return msg;
    }

    public void reload() {
        // Skopiuj pliki językowe z JAR do data folder jeśli nie istnieją
        saveDefaultLang("pl");
        saveDefaultLang("en");

        String langCode = plugin.getConfig().getString("planets.lang", "pl").toLowerCase();

        // Załaduj wybrany język
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");
        if (langFile.exists()) {
            lang = YamlConfiguration.loadConfiguration(langFile);
            log.info("[LangManager] Załadowano język: " + langCode);
        } else {
            log.warning("[LangManager] Nie znaleziono pliku lang/" + langCode
                    + ".yml — używam wbudowanego pl.yml jako fallback.");
            lang = null;
        }

        // Zawsze załaduj pl.yml z JAR jako fallback
        fallback = loadFromJar("lang/pl.yml");
    }

    private void saveDefaultLang(String code) {
        File target = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!target.exists()) {
            plugin.saveResource("lang/" + code + ".yml", false);
            log.info("[LangManager] Skopiowano domyślny plik językowy: lang/" + code + ".yml");
        }
    }


    private YamlConfiguration loadFromJar(String resourcePath) {
        InputStream is = plugin.getResource(resourcePath);
        if (is == null) {
            log.warning("[LangManager] Zasób nie znaleziony w JAR: " + resourcePath);
            return new YamlConfiguration();
        }
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            log.warning("[LangManager] Błąd ładowania zasobu " + resourcePath + ": " + e.getMessage());
            return new YamlConfiguration();
        }
    }
}