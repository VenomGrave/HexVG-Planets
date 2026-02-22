package com.venomgrave.hexvg.manager;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.recipe.RecipeManager;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class EnvironmentManager {

    private final HexVGPlanets plugin;
    private int taskId = -1;

    public EnvironmentManager(HexVGPlanets plugin) {
        this.plugin = plugin;

        int period = ConfigManager.getRulesEnvironmentPeriod(plugin);
        if (period > 0) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    plugin,
                    new DamagePlayers(plugin),
                    10L,
                    period * 20L
            );
            plugin.getLogger().info("EnvironmentManager aktywny (okres=" + period + "s, task=" + taskId + ")");
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }


    private class DamagePlayers implements Runnable {
        private final HexVGPlanets plugin;
        private final Map<UUID, Integer>     thirsts   = new HashMap<>();
        private final Map<UUID, PlayerState> mosquitos = new HashMap<>();
        private final Map<UUID, PlayerState> leeches   = new HashMap<>();
        private final Random random = new Random();
        DamagePlayers(HexVGPlanets plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            for (World world : Bukkit.getWorlds()) {
                if (!plugin.isPlanetWorld(world)) continue;
                switch (plugin.getPlanetType(world)) {
                    case HOTH:     freeze(world);                  break;
                    case TATOOINE: heat(world);                    break;
                    case DAGOBAH:  mosquito(world); leech(world);  break;
                    case MUSTAFAR: lavaBurn(world);                break;
                }
            }
        }


        private void freeze(World world) {
            boolean storm = world.hasStorm();

            for (Player player : world.getPlayers()) {
                GameMode gm = player.getGameMode();
                if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
                if (!player.hasPermission("hexvg.planets.env.freeze")) continue;
                if (hasWarmSuit(player)) continue;

                int    damage      = ConfigManager.getRulesFreezeDamage(plugin, player.getLocation());
                int    stormDamage = ConfigManager.getRulesFreezeStormdamage(plugin, player.getLocation());
                String message     = plugin.getLangManager().get("hoth.freeze");

                Block above = world.getBlockAt(
                        player.getLocation().getBlockX(),
                        player.getLocation().getBlockY() + 1,
                        player.getLocation().getBlockZ()
                );

                int realDamage = 0;

                if (storm && stormDamage > 0 && above.getLightFromSky() > 8) {
                    realDamage += stormDamage;
                }

                if (damage > 0) {
                    realDamage += damage;
                }

                if (realDamage > 0) {
                    plugin.sendMessage(player, message);
                    player.damage(realDamage);
                }
            }
        }


        private void heat(World world) {
            for (Player player : world.getPlayers()) {
                GameMode gm = player.getGameMode();
                UUID uuid = player.getUniqueId();

                int    damage = ConfigManager.getRulesHeatDamage(plugin, player.getLocation());
                String msg1   = plugin.getLangManager().get("tatooine.heat.water");
                String msg2   = plugin.getLangManager().get("tatooine.heat.thirsty");
                String msg3   = plugin.getLangManager().get("tatooine.heat.very_thirsty");
                String msg4   = plugin.getLangManager().get("tatooine.heat.dying");

                int thirst = thirsts.getOrDefault(uuid, 100);

                if (gm != GameMode.CREATIVE
                        && player.hasPermission("hexvg.planets.env.heat")
                        && !hasCoolingSuit(player)) {

                    if (damage > 0) {
                        Location loc  = player.getLocation();
                        Block    above = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
                        Block    at    = world.getBlockAt(loc.getBlockX(), loc.getBlockY(),     loc.getBlockZ());

                        if (at.getType() == Material.WATER) {
                            if (thirst != 100) {
                                thirst = 100;
                                plugin.sendMessage(player, msg1); // woda gasi pragnienie
                            }
                        } else {
                            thirst -= 2;
                            if (thirst == 50) plugin.sendMessage(player, msg2);
                            if (thirst == 25) plugin.sendMessage(player, msg3);

                            if (thirst <= 0) {
                                if (player.getHealth() - damage <= 0) {
                                    thirst = 100; // reset przed śmiercią
                                }
                                plugin.sendMessage(player, msg4);
                                player.damage(damage);
                            }
                        }

                        thirst = Math.max(0, Math.min(100, thirst));
                        thirsts.put(uuid, Integer.valueOf(thirst));
                    }

                } else {
                    thirsts.put(uuid, 100); // reset gdy bezpieczny
                }
            }
        }


        private void mosquito(World world) {
            for (Player player : world.getPlayers()) {
                GameMode gm = player.getGameMode();
                UUID uuid = player.getUniqueId();

                int    damage  = ConfigManager.getRulesMosquitoDamage(plugin, player.getLocation());
                int    rarity  = ConfigManager.getRulesMosquitoRarity(plugin, player.getLocation());
                int    runFree = ConfigManager.getRulesMosquitoRunFree(plugin, player.getLocation());
                String msg1    = plugin.getLangManager().get("dagobah.mosquito.lost");
                String msg2    = plugin.getLangManager().get("dagobah.mosquito.hear");
                String msg3    = plugin.getLangManager().get("dagobah.mosquito.see");
                String msg4    = plugin.getLangManager().get("dagobah.mosquito.attack");

                PlayerState mosquito = mosquitos.computeIfAbsent(uuid, k -> new PlayerState(player.getLocation()));

                if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR
                        || !player.hasPermission("hexvg.planets.env.mosquito")
                        || hasRepellentSuit(player)) {
                    mosquito.reset(player.getLocation());
                    continue;
                }

                if (damage <= 0) continue;

                Location loc   = player.getLocation();
                Block    above = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
                double   dist  = getDistance(loc, mosquito.location);

                if (above.getType() == Material.WATER
                        || (mosquito.stage != 0 && dist > runFree)) {
                    if (mosquito.stage != 0) {
                        mosquito.stage = 0;
                        plugin.sendMessage(player, msg1);
                    }
                    continue;
                }

                switch (mosquito.stage) {
                    case 0: // spokój
                        Block under = world.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                        if (under.getType().isSolid()) {
                            if (random.nextInt(20 * rarity) == 1) {
                                mosquito.stage    = 1;
                                mosquito.ctr      = 0;
                                mosquito.location = loc;
                                plugin.sendMessage(player, msg2);
                            }
                        }
                        break;

                    case 1:
                        mosquito.ctr++;
                        mosquito.location = moveCloser(loc, mosquito.location, 0.7);
                        if (mosquito.ctr > 5 || random.nextInt(20) == 1) {
                            mosquito.stage = 2;
                            mosquito.ctr   = 0;
                            plugin.sendMessage(player, msg3);
                        }
                        break;

                    case 2:
                        mosquito.ctr++;
                        mosquito.location = moveCloser(loc, mosquito.location, 0.7);
                        if (mosquito.ctr > 3 || random.nextInt(10) == 1) {
                            mosquito.stage = 3;
                            mosquito.ctr   = 0;
                        }
                        break;

                    case 3:
                        mosquito.location = moveCloser(loc, mosquito.location, 0.9);
                        if (player.getHealth() - damage <= 0) {
                            mosquito.stage = 0;
                        }
                        plugin.sendMessage(player, msg4);
                        player.damage(damage);
                        break;
                }
            }
        }


        private void leech(World world) {
            for (Player player : world.getPlayers()) {
                GameMode gm = player.getGameMode();
                UUID uuid = player.getUniqueId();

                int    damage = ConfigManager.getRulesLeechDamage(plugin, player.getLocation());
                int    rarity = ConfigManager.getRulesMosquitoRarity(plugin, player.getLocation());
                String msg1   = plugin.getLangManager().get("dagobah.leech.lost");
                String msg2   = plugin.getLangManager().get("dagobah.leech.sense");
                String msg3   = plugin.getLangManager().get("dagobah.leech.see");
                String msg4   = plugin.getLangManager().get("dagobah.leech.attack");
                String msg5   = plugin.getLangManager().get("dagobah.leech.run");

                PlayerState leech = leeches.computeIfAbsent(uuid,
                        k -> new PlayerState(player.getLocation()));

                if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR
                        || !player.hasPermission("hexvg.planets.env.leech")
                        || hasRepellentSuit(player)) {
                    leech.reset(player.getLocation());
                    continue;
                }

                if (damage <= 0) continue;

                Location loc  = player.getLocation();
                Block    at   = world.getBlockAt(loc.getBlockX(), loc.getBlockY(),     loc.getBlockZ());
                Block    head = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
                boolean  inW  = isWater(at) || isWater(head);

                if (leech.stage != 0
                        && leech.location.getWorld() != null
                        && !leech.location.getWorld().equals(world)) {
                    leech.stage = 0;
                    plugin.sendMessage(player, msg1);
                }

                switch (leech.stage) {
                    case 0: // spokój
                        if (inW && random.nextInt(20 * rarity) == 1) {
                            leech.stage    = 1;
                            leech.ctr      = 0;
                            leech.location = loc;
                            plugin.sendMessage(player, msg2);
                        }
                        break;

                    case 1:
                        if (inW) {
                            leech.ctr++;
                            if (leech.ctr > 4 || random.nextInt(8) == 1) {
                                leech.stage = 2;
                                leech.ctr   = 0;
                                plugin.sendMessage(player, msg3); // widzisz pijawki
                            }
                        } else {
                            leech.stage = 0;
                        }
                        break;

                    case 2:
                        if (inW) {
                            leech.ctr++;
                            if (leech.ctr > 4 || random.nextInt(4) == 1) {
                                leech.stage = 3;
                                leech.ctr   = 50;
                            }
                        } else {
                            leech.stage = 0;
                        }
                        break;

                    case 3:
                        if (leech.ctr < 0) {
                            leech.stage = 0;
                            plugin.sendMessage(player, msg1);
                        } else {
                            if (inW) {
                                leech.ctr = Math.min(100, leech.ctr + 5);
                                if (player.getHealth() - damage <= 0) {
                                    leech.stage = 0;
                                }
                                plugin.sendMessage(player, msg4);
                                player.damage(damage);
                            } else {
                                leech.ctr -= player.isSprinting() ? 20 : 5;
                                plugin.sendMessage(player, msg5);
                            }

                            player.setFoodLevel(Math.max(0, player.getFoodLevel() - damage));
                        }
                        break;
                }
            }
        }


        private void lavaBurn(World world) {
            for (Player player : world.getPlayers()) {
                GameMode gm = player.getGameMode();
                if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
                if (!player.hasPermission("hexvg.planets.env.lavaburn")) continue;
                if (!ConfigManager.isRulesLavaBurn(plugin, player.getLocation())) continue;

                boolean glassSuit = hasGlassSuit(player);

                if (!glassSuit) {
                    double hp = player.getHealth();
                    if (hp > 1.0) {
                        player.damage(0.5);
                        player.sendMessage(plugin.getLangManager().get("mustafar.heat"));
                    }
                }

                Location loc = player.getLocation();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();

                int cnt = 0;
                for (int xx = x - 3; xx <= x + 3; xx++) {
                    for (int zz = z - 3; zz <= z + 3; zz++) {
                        for (int yy = y - 2; yy <= y + 2; yy++) {
                            Material m = world.getBlockAt(xx, yy, zz).getType();
                            if (m == Material.LAVA) {
                                cnt++;
                            }
                        }
                    }
                }

                if (cnt > 0) {
                    if (glassSuit) {
                        if (random.nextInt(10) == 0) {
                            player.setFireTicks(Math.max(player.getFireTicks(), cnt / 10 + 20));
                        }
                    } else {
                        int fireTicks = Math.max(player.getFireTicks(), 20 + cnt * 3);
                        player.setFireTicks(fireTicks);
                        player.damage(1.0);
                        player.sendMessage(plugin.getLangManager().get("mustafar.lava_near"));
                    }
                }
            }
        }


        private boolean hasSuit(Player player, String tag) {
            if (!ConfigManager.getRulesEnvironmentSuit(plugin, player.getLocation())) return false;
            PlayerInventory inv = player.getInventory();
            return tagMatches(inv.getHelmet(),     tag)
                    && tagMatches(inv.getChestplate(), tag)
                    && tagMatches(inv.getLeggings(),   tag)
                    && tagMatches(inv.getBoots(),      tag);
        }

        private boolean tagMatches(ItemStack item, String tag) {
            if (item == null || !item.hasItemMeta()) return false;
            ItemMeta meta = item.getItemMeta();
            if (!meta.hasLore()) return false;
            List<String> lore = meta.getLore();
            return !lore.isEmpty() && tag.equals(lore.get(0));
        }

        private boolean hasWarmSuit(Player p)      { return hasSuit(p, RecipeManager.WARM_SUIT_TAG); }
        private boolean hasCoolingSuit(Player p)   { return hasSuit(p, RecipeManager.COOLING_SUIT_TAG); }
        private boolean hasRepellentSuit(Player p) { return hasSuit(p, RecipeManager.REPELLENT_SUIT_TAG); }
        private boolean hasGlassSuit(Player p)     { return hasSuit(p, RecipeManager.GLASS_SUIT_TAG); }


        private Location moveCloser(Location player, Location swarm, double factor) {
            double dx = player.getX() - swarm.getX();
            double dy = player.getY() - swarm.getY();
            double dz = player.getZ() - swarm.getZ();
            return new Location(swarm.getWorld(),
                    player.getX() + dx * factor,
                    player.getY() + dy * factor,
                    player.getZ() + dz * factor);
        }

        private double getDistance(Location a, Location b) {
            if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) return 9999;
            double dx = a.getX() - b.getX();
            double dy = a.getY() - b.getY();
            double dz = a.getZ() - b.getZ();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        private boolean isWater(Block block) {
            switch (block.getType()) {
                case WATER:
                case BUBBLE_COLUMN:
                case KELP:
                case KELP_PLANT:
                case SEAGRASS:
                case TALL_SEAGRASS:
                    return true;
                default:
                    return false;
            }
        }
    }


    private static class PlayerState {
        Location location;
        int stage;
        int ctr;

        PlayerState(Location location) {
            this.location = location;
            this.stage    = 0;
            this.ctr      = 0;
        }

        void reset(Location loc) {
            this.location = loc;
            this.stage    = 0;
            this.ctr      = 0;
        }
    }
}