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
import org.bukkit.event.block.BlockPlaceEvent;


public class BlockPlaceListener implements Listener {

    private final HexVGPlanets plugin;

    public BlockPlaceListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Block block = event.getBlock();
        World world = block.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);
        if (type != PlanetType.MUSTAFAR) return;

        Material mat = block.getType();
        if (!PlanetUtils.isTooHot(block.getLocation(), 2)) return;



        if (mat == Material.GRASS_BLOCK) {

            plugin.getTaskQueue().add(new BlockReplaceTask(
                    world, block.getX(), block.getY(), block.getZ(),
                    Material.GRASS_BLOCK, Material.DIRT));
        } else if (mat == Material.MYCELIUM) {
            plugin.getTaskQueue().add(new BlockReplaceTask(
                    world, block.getX(), block.getY(), block.getZ(),
                    Material.MYCELIUM, Material.DIRT));
        } else if (isFlammablePlant(mat)) {

            event.setCancelled(true);
        }
    }



    private boolean isFlammablePlant(Material mat) {
        switch (mat) {

            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case DANDELION:
            case POPPY:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case SUNFLOWER:
            case LILAC:
            case ROSE_BUSH:
            case PEONY:
            case TALL_GRASS:
            case LARGE_FERN:
            case CACTUS:
            case SUGAR_CANE:
            case BAMBOO:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case OAK_SAPLING:
            case SPRUCE_SAPLING:
            case BIRCH_SAPLING:
            case JUNGLE_SAPLING:
            case ACACIA_SAPLING:
            case DARK_OAK_SAPLING:
            case MANGROVE_PROPAGULE:
            case VINE:
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
                return true;
            default:
                return false;
        }
    }
}