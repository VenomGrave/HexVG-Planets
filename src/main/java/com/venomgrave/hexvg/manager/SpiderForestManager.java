package com.venomgrave.hexvg.manager;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SpiderForestManager {

    private final HexVGPlanets plugin;
    private BukkitTask task;
    private final Random random = new Random();
    private final Map<UUID, Long> lastSpawn = new HashMap<>();

    public SpiderForestManager(HexVGPlanets plugin) {
        this.plugin = plugin;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 60L, 60L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    public boolean isSpiderForest(Location loc) {
        if (!plugin.isPlanetWorld(loc.getWorld())) return false;
        if (plugin.getPlanetType(loc.getWorld()) != PlanetType.DAGOBAH) return false;
        return plugin.getRegionManager().hasFlag(loc, "spiderforest");
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (World world : Bukkit.getWorlds()) {
            if (!plugin.isPlanetWorld(world)) continue;
            if (plugin.getPlanetType(world) != PlanetType.DAGOBAH) continue;

            for (Player player : world.getPlayers()) {
                if (!isSpiderForest(player.getLocation())) continue;
                UUID id = player.getUniqueId();
                long last = lastSpawn.getOrDefault(id, 0L);
                if (now - last < 8000) continue;

                if (random.nextInt(3) == 0) {
                    spawnSpider(world, player.getLocation());
                    lastSpawn.put(id, now);
                }
            }
        }
    }

    private void spawnSpider(World world, Location near) {
        int rx = near.getBlockX() + random.nextInt(24) - 12;
        int rz = near.getBlockZ() + random.nextInt(24) - 12;
        int ry = world.getHighestBlockYAt(rx, rz) + 1;
        world.spawnEntity(new Location(world, rx, ry, rz), EntityType.SPIDER);
    }
}