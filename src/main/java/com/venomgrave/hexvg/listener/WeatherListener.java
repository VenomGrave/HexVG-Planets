package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {

    private final HexVGPlanets plugin;

    public WeatherListener(HexVGPlanets plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);

        if (type == PlanetType.TATOOINE || type == PlanetType.MUSTAFAR) {
            if (event.toWeatherState()) {
                event.setCancelled(true);
                world.setStorm(false);
                world.setThundering(false);
            }
        }

        if (type == PlanetType.HOTH) {
            if (!event.toWeatherState()) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) {
        World world = event.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);

        if (event.toThunderState()) {
            event.setCancelled(true);
            world.setThundering(false);
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (!plugin.isPlanetWorld(world)) return;

        LightningStrikeEvent.Cause cause = event.getCause();
        if (cause != LightningStrikeEvent.Cause.CUSTOM) {
            event.setCancelled(true);
        }
    }

    public void applyInitialWeather(World world) {
        if (!plugin.isPlanetWorld(world)) return;

        PlanetType type = plugin.getPlanetType(world);
        switch (type) {
            case HOTH:
                world.setStorm(true);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                break;
            case TATOOINE:
            case MUSTAFAR:
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                break;
            case DAGOBAH:
                world.setThundering(false);
                break;
        }
    }
}