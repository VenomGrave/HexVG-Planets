package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerJoinListener implements Listener {

    private static final String META_VISITED = "hexvg.visited";

    private static final int LANDING_EFFECT_TICKS = 40;

    private final HexVGPlanets plugin;

    public PlayerJoinListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        handlePlanetEntry(player, world, true);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player  = event.getPlayer();
        World newWorld = player.getWorld();
        World oldWorld = event.getFrom();

        boolean leftPlanet   = plugin.isPlanetWorld(oldWorld)  && !plugin.isPlanetWorld(newWorld);
        boolean enteredPlanet = plugin.isPlanetWorld(newWorld) && !plugin.isPlanetWorld(oldWorld);
        boolean changedPlanet = plugin.isPlanetWorld(newWorld) && plugin.isPlanetWorld(oldWorld);

        if (leftPlanet) {
            handlePlanetExit(player, oldWorld);
        } else if (enteredPlanet) {
            handlePlanetEntry(player, newWorld, false);
        } else if (changedPlanet) {
            handlePlanetExit(player, oldWorld);
            handlePlanetEntry(player, newWorld, false);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.SLOW);
        event.getPlayer().removePotionEffect(PotionEffectType.DARKNESS);
    }


    private void handlePlanetEntry(Player player, World world, boolean fromJoin) {PlanetType type = plugin.getPlanetType(world);
        String planetName = getPlanetDisplayName(type);

        player.sendMessage(org.bukkit.ChatColor.GOLD + "★ " +
                org.bukkit.ChatColor.YELLOW + "Wylądowałeś na planecie " +
                org.bukkit.ChatColor.WHITE + planetName +
                org.bukkit.ChatColor.YELLOW + ".");


        String suitHint = getSuitHint(type);
        if (suitHint != null) {
            player.sendMessage(org.bukkit.ChatColor.GRAY + "  ► " + suitHint);
        }

        Sound landSound = getLandingSound(type);
        if (landSound != null) {
            player.playSound(player.getLocation(), landSound, 0.8f, 1.0f);
        }

        if (!fromJoin) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW, LANDING_EFFECT_TICKS, 1, false, false, false));
            if (type == PlanetType.MUSTAFAR) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DARKNESS, LANDING_EFFECT_TICKS, 0, false, false, false));
            }
        }

        markVisited(player, type);

        if (ConfigManager.isDebug(plugin)) {
            plugin.getLogger().info("[Debug] " + player.getName() +
                    " entered " + type.toKey() + " (" + world.getName() + ")");
        }
    }


    private void handlePlanetExit(Player player, World world) {
        PlanetType type = plugin.getPlanetType(world);

        player.sendMessage(org.bukkit.ChatColor.GRAY + "Opuściłeś planetę " +
                getPlanetDisplayName(type) + ".");

        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.DARKNESS);
    }


    private String getPlanetDisplayName(PlanetType type) {
        switch (type) {
            case HOTH:     return "§bHoth";
            case TATOOINE: return "§eTabuine";
            case DAGOBAH:  return "§2Dagobah";
            case MUSTAFAR: return "§cMustafar";
            default:       return type.toKey();
        }
    }

    private String getSuitHint(PlanetType type) {
        switch (type) {
            case HOTH:
                return "§7Potrzebujesz §fWarm Suit §7aby przeżyć mróz.";
            case TATOOINE:
                return "§7Potrzebujesz §fCooling Suit §7aby przeżyć upał.";
            case DAGOBAH:
                return "§7Potrzebujesz §fRepellent Suit §7aby odegnać insekty.";
            case MUSTAFAR:
                return "§7Potrzebujesz §fGlass Suit §7aby przeżyć żar lawy.";
            default:
                return null;
        }
    }

    private Sound getLandingSound(PlanetType type) {
        switch (type) {
            case HOTH:
                return Sound.BLOCK_POWDER_SNOW_PLACE;
            case TATOOINE:
                return Sound.BLOCK_SAND_PLACE;
            case DAGOBAH:
                return Sound.BLOCK_GRASS_PLACE;
            case MUSTAFAR:
                return Sound.BLOCK_LAVA_AMBIENT;
            default:
                return null;
        }
    }

    private void markVisited(Player player, PlanetType type) {
        String key = META_VISITED + "." + type.toKey();
        player.getPersistentDataContainer();
    }
}