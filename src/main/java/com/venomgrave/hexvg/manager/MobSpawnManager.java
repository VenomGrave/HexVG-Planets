package com.venomgrave.hexvg.manager;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class MobSpawnManager {

    private final HexVGPlanets plugin;
    private BukkitTask task;
    private final Random random = new Random();

    public MobSpawnManager(HexVGPlanets plugin) {
        this.plugin = plugin;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 100L, 100L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    private void tick() {
        for (World world : Bukkit.getWorlds()) {
            if (!plugin.isPlanetWorld(world)) continue;
            if (!plugin.getConfigManager().getBooleanForWorld(world.getName(), "rules.spawn.neutral.on", true)) continue;

            int rarity = plugin.getConfigManager().getIntForWorld(world.getName(), "rules.spawn.neutral.rarity", 2);
            if (random.nextInt(rarity * 5 + 1) != 0) continue;

            String mobList = plugin.getConfigManager().getStringForWorld(
                    world.getName(), "rules.spawn.neutral.mobs", "chicken,cow");
            String[] mobs = mobList.split(",");
            if (mobs.length == 0) continue;

            String chosenName = mobs[random.nextInt(mobs.length)].trim().toUpperCase();
            EntityType type;
            try {
                type = EntityType.valueOf(chosenName);
            } catch (IllegalArgumentException e) {
                continue;
            }

            world.getPlayers().forEach(player -> {
                if (random.nextInt(3) != 0) return;
                int rx = player.getLocation().getBlockX() + random.nextInt(64) - 32;
                int rz = player.getLocation().getBlockZ() + random.nextInt(64) - 32;
                int ry = world.getHighestBlockYAt(rx, rz) + 1;
                Location loc = new Location(world, rx, ry, rz);
                try { world.spawnEntity(loc, type); } catch (Exception ignored) {}
            });
        }
    }
}