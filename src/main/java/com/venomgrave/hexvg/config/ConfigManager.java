package com.venomgrave.hexvg.config;

import com.venomgrave.hexvg.HexVGPlanets;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final HexVGPlanets plugin;

    public ConfigManager(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    // ----------------------------------------------------------------
    // Generic getter (config path under "planets.")
    // ----------------------------------------------------------------

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public boolean getBoolean(String key, boolean def) {
        return cfg().getBoolean("planets." + key, def);
    }

    public int getInt(String key, int def) {
        return cfg().getInt("planets." + key, def);
    }

    public String getString(String key, String def) {
        return cfg().getString("planets." + key, def);
    }

    // ----------------------------------------------------------------
    // Per-world override (from worldConfig.yml, falls back to global)
    // ----------------------------------------------------------------

    public boolean getBooleanForWorld(String worldName, String key, boolean def) {
        String val = plugin.getWorldConfigManager().getFlag(worldName, key);
        if (val == null) return getBoolean(key, def);
        return Boolean.parseBoolean(val);
    }

    public int getIntForWorld(String worldName, String key, int def) {
        String val = plugin.getWorldConfigManager().getFlag(worldName, key);
        if (val == null) return getInt(key, def);
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }

    public String getStringForWorld(String worldName, String key, String def) {
        String val = plugin.getWorldConfigManager().getFlag(worldName, key);
        return val != null ? val : getString(key, def);
    }

    // ----------------------------------------------------------------
    // Convenience static accessors (used by generators / listeners)
    // Mirrors original ConfigManager.isXxx() naming.
    // ----------------------------------------------------------------

    public static int getWorldSurfaceOffset(HexVGPlanets plugin, String worldName) {
        return plugin.getConfigManager().getIntForWorld(worldName, "world.surfaceoffset", 0);
    }

    public static boolean isSmoothSnow(HexVGPlanets plugin) {
        return plugin.getConfigManager().getBoolean("smoothsnow", true);
    }

    public static boolean isSmoothLava(HexVGPlanets plugin) {
        return plugin.getConfigManager().getBoolean("smoothlava", true);
    }

    public static boolean isDebug(HexVGPlanets plugin) {
        return plugin.getConfigManager().getBoolean("debug", false);
    }

    public static boolean isGenerateLogs(HexVGPlanets plugin, org.bukkit.World world) {
        return plugin.getConfigManager().getBooleanForWorld(world.getName(), "generate.logs", true);
    }

    public static boolean isGenerateOres(HexVGPlanets plugin, org.bukkit.World world) {
        return plugin.getConfigManager().getBooleanForWorld(world.getName(), "generate.ores", true);
    }

    public static boolean isRulesDropice(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.dropice", true);
    }

    public static boolean isRulesDroppackedice(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.droppackedice", true);
    }

    public static boolean isRulesDropsnow(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.dropsnow", true);
    }

    public static boolean isRulesFreezewater(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.freezewater", true);
    }

    public static boolean isRulesFreezelava(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.freezelava", true);
    }

    public static boolean isRulesStopMelt(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.stopmelt", true);
    }

    public static boolean isRulesLessStone(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.lessstone", true);
    }

    public static boolean isRulesLavaBurn(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.lavaburn", true);
    }

    public static boolean isRulesPlantsGrow(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.plantsgrow", false);
    }

    public static boolean isRulesGrassSpread(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.grassspread", false);
    }

    public static boolean getRulesEnvironmentSuit(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getBooleanForWorld(loc.getWorld().getName(), "rules.environment.suit", true);
    }

    public static int getRulesEnvironmentPeriod(HexVGPlanets plugin) {
        return plugin.getConfigManager().getInt("rules.environment.period", 2);
    }

    public static int getRulesFreezeDamage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.freeze.damage", 2);
    }

    public static int getRulesFreezeStormdamage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.freeze.stormdamage", 1);
    }

    public static String getRulesFreezeMessage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.freeze.message", "&bZamarzasz!");
    }

    public static int getRulesHeatDamage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.heat.damage", 2);
    }

    public static String getRulesHeatMessage1(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.heat.message1", "&6Woda gasi pragnienie.");
    }

    public static String getRulesHeatMessage2(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.heat.message2", "&6Zaczynasz czuć pragnienie.");
    }

    public static String getRulesHeatMessage3(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.heat.message3", "&6Jesteś bardzo spragniony.");
    }

    public static String getRulesHeatMessage4(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.heat.message4", "&6Znajdź wodę!");
    }

    public static int getRulesMosquitoDamage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.mosquito.damage", 1);
    }

    public static int getRulesMosquitoRarity(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.mosquito.rarity", 5);
    }

    public static int getRulesMosquitoRunFree(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.mosquito.runfree", 10);
    }

    public static String getRulesMosquitoMessage1(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.mosquito.message1", "&2Zgubiłeś rój.");
    }

    public static String getRulesMosquitoMessage2(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.mosquito.message2", "&2Słyszysz brzęczenie.");
    }

    public static String getRulesMosquitoMessage3(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.mosquito.message3", "&2Widzisz komary.");
    }

    public static String getRulesMosquitoMessage4(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.mosquito.message4", "&2Komary atakują! Uciekaj!");
    }

    public static int getRulesLeechDamage(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getIntForWorld(loc.getWorld().getName(), "rules.leech.damage", 1);
    }

    public static String getRulesLeechMessage1(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.leech.message1", "&2Pozbyłeś się pijawek.");
    }

    public static String getRulesLeechMessage2(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.leech.message2", "&2Coś porusza się w wodzie.");
    }

    public static String getRulesLeechMessage3(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.leech.message3", "&2Widzisz pijawki.");
    }

    public static String getRulesLeechMessage4(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.leech.message4", "&2Pijawki atakują! Wyjdź z wody!");
    }

    public static String getRulesLeechMessage5(HexVGPlanets plugin, Location loc) {
        return plugin.getConfigManager().getStringForWorld(loc.getWorld().getName(), "rules.leech.message5", "&2Biegnij żeby je strząsnąć!");
    }

    public static boolean getSchematicEntity(HexVGPlanets plugin, org.bukkit.World world) {
        return plugin.getConfigManager().getBooleanForWorld(world.getName(), "rules.spawn.schematic_entity", true);
    }

    public static int getStructureRarity(HexVGPlanets plugin, org.bukkit.World world, String structureKey) {
        return plugin.getConfigManager().getIntForWorld(world.getName(), "structure." + structureKey + ".rarity", 2);
    }
}