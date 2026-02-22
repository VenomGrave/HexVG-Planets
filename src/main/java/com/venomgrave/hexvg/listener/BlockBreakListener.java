package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class BlockBreakListener implements Listener {

    private final HexVGPlanets plugin;
    private final Random random = new Random();

    public BlockBreakListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Block block = event.getBlock();
        World world = block.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);
        Material mat    = block.getType();

        if (type == PlanetType.HOTH) {


            if (mat == Material.ICE
                    && ConfigManager.isRulesDropice(plugin, block.getLocation())) {
                event.setDropItems(false);
                block.setType(Material.AIR, false);
                dropOne(world, block, Material.ICE);
            } else if (mat == Material.SNOW_BLOCK
                    && ConfigManager.isRulesDropsnow(plugin, block.getLocation())) {
                event.setDropItems(false);
                block.setType(Material.AIR, false);
                dropOne(world, block, Material.SNOW_BLOCK);
            }


            if (mat == Material.PACKED_ICE
                    && ConfigManager.isRulesDroppackedice(plugin, block.getLocation())) {
                event.setDropItems(false);
                block.setType(Material.AIR, false);
                dropOne(world, block, Material.PACKED_ICE);
            }
        }

        if (type == PlanetType.MUSTAFAR) {
            if (mat == Material.STONE
                    && ConfigManager.isRulesLessStone(plugin, block.getLocation())) {
                event.setDropItems(false);
                block.setType(Material.AIR, false);
                if (random.nextInt(8) == 1) {
                    Material drop = random.nextInt(4) > 0 ? Material.COBBLESTONE : Material.STONE;
                    dropOne(world, block, drop);
                }
            }
        }
    }

    private void dropOne(World world, Block block, Material mat) {
        world.dropItemNaturally(block.getLocation(), new ItemStack(mat, 1));
    }
}