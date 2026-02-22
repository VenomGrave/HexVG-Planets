package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.task.BlockReplaceTask;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class BucketListener implements Listener {

    private final HexVGPlanets plugin;

    public BucketListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;

        Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());
        World world = targetBlock.getWorld();

        if (!plugin.isPlanetWorld(world)) return;
        if (plugin.getPlanetType(world) != PlanetType.HOTH) return;

        if (PlanetUtils.canPlaceLiquid(plugin, world, targetBlock)) return;

        Material bucket = event.getBucket();

        if (isWaterBucket(bucket)
                && ConfigManager.isRulesFreezewater(plugin, targetBlock.getLocation())) {

            plugin.getTaskQueue().add(new BlockReplaceTask(
                    world,
                    targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(),
                    Material.WATER, Material.ICE));

        } else if (bucket == Material.LAVA_BUCKET
                && ConfigManager.isRulesFreezelava(plugin, targetBlock.getLocation())) {

            plugin.getTaskQueue().add(new BlockReplaceTask(
                    world,
                    targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(),
                    Material.LAVA, Material.STONE));
        }
    }


    private boolean isWaterBucket(Material m) {
        switch (m) {
            case WATER_BUCKET:
            case COD_BUCKET:
            case SALMON_BUCKET:
            case PUFFERFISH_BUCKET:
            case TROPICAL_FISH_BUCKET:
            case AXOLOTL_BUCKET:
            case TADPOLE_BUCKET:   // nowe w 1.19
                return true;
            default:
                return false;
        }
    }
}