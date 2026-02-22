package com.venomgrave.hexvg;

import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.config.LangManager;
import com.venomgrave.hexvg.config.WorldConfigManager;
import com.venomgrave.hexvg.generator.PlanetChunkGenerator;
import com.venomgrave.hexvg.listener.*;
import com.venomgrave.hexvg.loot.LootGenerator;
import com.venomgrave.hexvg.manager.*;
import com.venomgrave.hexvg.recipe.RecipeManager;
import com.venomgrave.hexvg.region.RegionManager;
import com.venomgrave.hexvg.schematic.SchematicRegistry;
import com.venomgrave.hexvg.task.TaskQueue;
import com.venomgrave.hexvg.util.MessageFormatter;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;


public class HexVGPlanets extends JavaPlugin {

    private static HexVGPlanets instance;

    public static HexVGPlanets getInstance() {
        return instance;
    }

    private ConfigManager configManager;
    private WorldConfigManager worldConfigManager;
    private LangManager langManager;
    private RegionManager regionManager;
    private SchematicRegistry schematicRegistry;
    private LootGenerator lootGenerator;
    private TaskQueue taskQueue;

    private BlockPlaceListener blockPlaceListener;
    private BlockBreakListener blockBreakListener;
    private BlockPhysicsListener blockPhysicsListener;
    private BucketListener bucketListener;
    private CreatureSpawnListener creatureSpawnListener;
    private EntityListener entityListener;
    private WeatherListener weatherListener;
    private PlayerJoinListener playerJoinListener;
    private PlanetTabCompleter tabCompleter;
    private EnvironmentManager environmentManager;
    private VolcanoManager volcanoManager;
    private MobSpawnManager mobSpawnManager;
    private SpiderForestManager spiderForestManager;


    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.worldConfigManager = new WorldConfigManager(this);
        this.langManager = new LangManager(this);

        this.schematicRegistry = new SchematicRegistry(this);
        this.schematicRegistry.loadAll();

        this.lootGenerator = new LootGenerator(this);
        this.lootGenerator.load();

        this.regionManager = new RegionManager(this);
        this.regionManager.load();

        this.taskQueue = new TaskQueue(this);
        this.taskQueue.start();

        this.blockPlaceListener    = new BlockPlaceListener(this);
        this.blockBreakListener    = new BlockBreakListener(this);
        this.blockPhysicsListener  = new BlockPhysicsListener(this);
        this.bucketListener        = new BucketListener(this);
        this.creatureSpawnListener = new CreatureSpawnListener(this);
        this.entityListener        = new EntityListener(this);

        getServer().getPluginManager().registerEvents(blockPlaceListener,    this);
        getServer().getPluginManager().registerEvents(blockBreakListener,    this);
        getServer().getPluginManager().registerEvents(blockPhysicsListener,  this);
        getServer().getPluginManager().registerEvents(bucketListener,        this);
        getServer().getPluginManager().registerEvents(creatureSpawnListener, this);
        getServer().getPluginManager().registerEvents(entityListener,        this);

        this.weatherListener    = new WeatherListener(this);
        this.playerJoinListener = new PlayerJoinListener(this);
        getServer().getPluginManager().registerEvents(weatherListener,    this);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        getServer().getPluginManager().registerEvents(new com.venomgrave.hexvg.listener.PlayerSelectionListener(this), this);

        this.tabCompleter = new PlanetTabCompleter(this);
        java.util.Arrays.asList(
                "planetsaddworld","planetsdelworld","planetssetworldtype",
                "planetssetworldflag","planetsworldinfo","planetspaste","planetsregion",
                "planetssave","planetspos1","planetspos2"
        ).forEach(cmd -> {
            var bc = getCommand(cmd);
            if (bc != null) bc.setTabCompleter(tabCompleter);
        });

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.environmentManager  = new EnvironmentManager(this);
            this.volcanoManager      = new VolcanoManager(this);
            this.mobSpawnManager     = new MobSpawnManager(this);
            this.spiderForestManager = new SpiderForestManager(this);

            if (weatherListener != null) {
                for (org.bukkit.World w : getServer().getWorlds()) {
                    weatherListener.applyInitialWeather(w);
                }
            }
        }, 1L);

        Bukkit.getScheduler().runTaskLater(this, () ->
                RecipeManager.registerAll(this), 1L);

        saveResourceSilently("custom/example.sm");
        saveResourceSilently("custom/example.ol");

        getLogger().info("HexVG-Planets enabled.");
    }

    @Override
    public void onDisable() {
        if (environmentManager  != null) environmentManager.stop();
        if (volcanoManager      != null) volcanoManager.stop();
        if (mobSpawnManager     != null) mobSpawnManager.stop();
        if (taskQueue           != null) taskQueue.stop();

        for (World world : getServer().getWorlds()) {
            if (isPlanetWorld(world)) {
                for (Player p : world.getPlayers()) {
                    p.kickPlayer("Server reloading");
                }
                getServer().unloadWorld(world, true);
            }
        }

        getLogger().info("HexVG-Planets disabled.");
    }


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        PlanetType type = PlanetType.fromString(id != null ? id : "");
        return new PlanetChunkGenerator(worldName, type);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return CommandHandler.handle(this, sender, cmd, args);
    }


    public boolean isPlanetWorld(World world) {
        return worldConfigManager.isRegistered(world.getName());
    }

    public boolean isPlanetWorld(String worldName) {
        return worldConfigManager.isRegistered(worldName);
    }

    public PlanetType getPlanetType(World world) {
        return worldConfigManager.getType(world.getName());
    }

    public PlanetType getPlanetType(String worldName) {
        return worldConfigManager.getType(worldName);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(MessageFormatter.colorize(message));
    }

    public ConfigManager getConfigManager()           { return configManager; }
    public WorldConfigManager getWorldConfigManager() { return worldConfigManager; }
    public LangManager getLangManager()               { return langManager; }
    public RegionManager getRegionManager()           { return regionManager; }
    public SchematicRegistry getSchematicRegistry()   { return schematicRegistry; }
    public LootGenerator getLootGenerator()           { return lootGenerator; }
    public TaskQueue getTaskQueue()                   { return taskQueue; }
    public SpiderForestManager getSpiderForestManager() { return spiderForestManager; }


    private void saveResourceSilently(String path) {
        try {
            saveResource(path, true);
        } catch (Exception ignored) { }
    }
}