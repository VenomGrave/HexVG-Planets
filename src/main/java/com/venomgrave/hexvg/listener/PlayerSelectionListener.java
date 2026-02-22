package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.PlayerSelectionManager;
import com.venomgrave.hexvg.util.MessageFormatter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerSelectionListener implements Listener {

    private static final Material DEFAULT_TOOL = Material.WOODEN_SWORD;

    private final HexVGPlanets plugin;

    public PlayerSelectionListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("hexvg.planets.paste")) return;

        Material tool = getConfiguredTool();
        if (player.getInventory().getItemInMainHand().getType() != tool) return;

        Action action = event.getAction();
        boolean isLeft  = action == Action.LEFT_CLICK_BLOCK  || action == Action.LEFT_CLICK_AIR;
        boolean isRight = action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR;

        if (!isLeft && !isRight) return;

        Location loc = null;
        if (event.getClickedBlock() != null) {
            loc = event.getClickedBlock().getLocation();
        } else {
            loc = player.getTargetBlockExact(10) != null
                    ? player.getTargetBlockExact(10).getLocation()
                    : player.getLocation();
        }
        if (loc == null) return;

        if (isLeft) {
            PlayerSelectionManager.setPos1(player, loc);
            MessageFormatter.sendInfo(player,
                    "Punkt &b1 &7ustawiony na " + MessageFormatter.formatCoords(loc.getX(), loc.getY(), loc.getZ()));
            event.setCancelled(true);
        } else {
            PlayerSelectionManager.setPos2(player, loc);
            MessageFormatter.sendInfo(player,
                    "Punkt &b2 &7ustawiony na " + MessageFormatter.formatCoords(loc.getX(), loc.getY(), loc.getZ()));
            event.setCancelled(true);
        }

        Location p1 = PlayerSelectionManager.getPos1(player);
        Location p2 = PlayerSelectionManager.getPos2(player);
        if (p1 != null && p2 != null && p1.getWorld() != null
                && p1.getWorld().equals(p2.getWorld())) {
            int w = Math.abs(p2.getBlockX() - p1.getBlockX()) + 1;
            int h = Math.abs(p2.getBlockY() - p1.getBlockY()) + 1;
            int l = Math.abs(p2.getBlockZ() - p1.getBlockZ()) + 1;
            MessageFormatter.sendInfo(player,
                    "Zaznaczenie: &f" + w + "x" + h + "x" + l +
                            " &7(" + (w * h * l) + " bloków). Użyj &f/planetssave <nazwa>&7 aby zapisać.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerSelectionManager.clearSelection(event.getPlayer());
    }

    private Material getConfiguredTool() {
        String name = plugin.getConfigManager().getString("selection.tool", "WOODEN_SWORD");
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return DEFAULT_TOOL; }
    }
}