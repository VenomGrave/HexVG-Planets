package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;


public class BlockPhysicsListener implements Listener {

    private final HexVGPlanets plugin;

    public BlockPhysicsListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);
        if (type != PlanetType.HOTH) return;

        Material mat = block.getType();
        boolean isMeltable = mat == Material.ICE
                || mat == Material.PACKED_ICE
                || mat == Material.BLUE_ICE
                || mat == Material.SNOW_BLOCK
                || mat == Material.SNOW;

        if (isMeltable && ConfigManager.isRulesStopMelt(plugin, block.getLocation())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockForm(BlockFormEvent event) {
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        if (!ConfigManager.isRulesPlantsGrow(plugin, block.getLocation())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent event) {
        World world = event.getLocation().getWorld();
        if (world == null || !plugin.isPlanetWorld(world)) return;

        if (!ConfigManager.isRulesPlantsGrow(plugin, event.getLocation())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block source = event.getSource();
        World world  = source.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        Material mat = source.getType();
        boolean isGrasslike = mat == Material.GRASS_BLOCK || mat == Material.MYCELIUM;

        if (isGrasslike && !ConfigManager.isRulesGrassSpread(plugin, source.getLocation())) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        World world = block.getWorld();
        if (!plugin.isPlanetWorld(world)) return;


    }
}