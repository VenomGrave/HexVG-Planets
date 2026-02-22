package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;


public class CreatureSpawnListener implements Listener {

    private final HexVGPlanets plugin;

    public CreatureSpawnListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;

        World world = event.getLocation().getWorld();
        if (world == null || !plugin.isPlanetWorld(world)) return;

        SpawnReason reason = event.getSpawnReason();

        if (reason == SpawnReason.CUSTOM || reason == SpawnReason.SPAWNER) return;

        PlanetType type = plugin.getPlanetType(world);

        switch (type) {

            case MUSTAFAR:
                blockMustafarSpawns(event, world, reason);
                break;

            case HOTH:
                limitSlimeSpawns(event, world);
                break;

            case TATOOINE:
            case DAGOBAH:
            default:
                // Brak specjalnych blokad
                break;
        }
    }



    private void blockMustafarSpawns(CreatureSpawnEvent event, World world, SpawnReason reason) {
        boolean allowSpawns = plugin.getConfigManager()
                .getBooleanForWorld(world.getName(), "rules.spawn.mustafar", false);

        if (!allowSpawns) {
            if (!isPluginControlledReason(reason)) {
                event.setCancelled(true);
            }
        }
    }


    private void limitSlimeSpawns(CreatureSpawnEvent event, World world) {
        if (event.getEntityType() != EntityType.SLIME) return;

        boolean limitSlime = plugin.getConfigManager()
                .getBooleanForWorld(world.getName(), "rules.limitslime", true);

        if (!limitSlime) return;

        int seaLevel = world.getSeaLevel();
        if (event.getLocation().getBlockY() > seaLevel - 10) {
            event.setCancelled(true);
        }
    }

    // ----------------------------------------------------------------

    private boolean isPluginControlledReason(SpawnReason reason) {
        switch (reason) {
            case CUSTOM:
            case SPAWNER:
            case COMMAND:
            case SPAWNER_EGG:
            case BUILD_SNOWMAN:
            case BUILD_IRONGOLEM:
            case BUILD_WITHER:
            case BREEDING:
            case CURED:
            case DISPENSE_EGG:
            case INFECTION:
            case REINFORCEMENTS:
                return true;
            default:
                return false;
        }
    }
}