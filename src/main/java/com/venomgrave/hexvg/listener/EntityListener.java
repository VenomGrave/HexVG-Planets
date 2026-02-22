package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;


public class EntityListener implements Listener {

    private final HexVGPlanets plugin;
    private final Random random = new Random();

    public EntityListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        World world   = entity.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);

        if (type == PlanetType.MUSTAFAR
                && !(entity instanceof Player)
                && entity instanceof Monster) {
            DamageCause cause = event.getCause();
            if (cause == DamageCause.LAVA || cause == DamageCause.FIRE
                    || cause == DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombustByBlock(EntityCombustByBlockEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        World world   = entity.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);

        if (type == PlanetType.MUSTAFAR
                && !(entity instanceof Player)
                && entity instanceof Monster) {
            event.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getWorld() == null) return;
        World world = event.getEntity().getWorld();
        if (!plugin.isPlanetWorld(world)) return;

    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        World world   = entity.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);

        if (type == PlanetType.MUSTAFAR) {
            Entity target = event.getTarget();
            if (target instanceof Player) {
                Player p = (Player) target;
                if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                    event.setCancelled(true);
                }
            }
        }
    }
}