package com.venomgrave.hexvg.manager;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class VolcanoManager {

    private final HexVGPlanets plugin;
    private BukkitTask task;
    private final Random random = new Random();

    public VolcanoManager(HexVGPlanets plugin) {
        this.plugin = plugin;
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 200L, 200L);
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    private void tick() {
        for (World world : Bukkit.getWorlds()) {
            if (!plugin.isPlanetWorld(world)) continue;
            if (plugin.getPlanetType(world) != PlanetType.MUSTAFAR) continue;
            if (!plugin.getConfigManager().getBooleanForWorld(world.getName(), "rules.volcanoes", true)) continue;

            world.getPlayers().forEach(player -> {
                if (random.nextInt(8) != 0) return;
                int rx = player.getLocation().getBlockX() + random.nextInt(128) - 64;
                int rz = player.getLocation().getBlockZ() + random.nextInt(128) - 64;
                erupt(world, rx, rz);
            });
        }
    }

    private void erupt(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Block top = world.getBlockAt(x, y, z);
        if (top.getType() != Material.STONE && top.getType() != Material.COBBLESTONE) return;

        int height = 3 + random.nextInt(6);
        for (int dy = 1; dy <= height; dy++) {
            Block b = world.getBlockAt(x, y + dy, z);
            if (b.getType() == Material.AIR) {
                b.setType(Material.LAVA, false);
                final Block fb = b;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (fb.getType() == Material.LAVA) fb.setType(Material.COBBLESTONE, false);
                }, 60L + random.nextInt(40));
            }
        }
    }
}