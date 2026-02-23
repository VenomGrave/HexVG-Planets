package com.venomgrave.hexvg.listener;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.world.PlanetType;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class BlockBreakListener implements Listener {

    private final HexVGPlanets plugin;
    private final Random random = new Random();

    // Rudy, które na Mustafarze mogą wymagać RFG
    private static final Set<Material> MUSTAFAR_ORES = EnumSet.of(
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE
    );

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

        // --- HOTH: dropy lodu/śniegu/pakowanego lodu ---
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

        // --- MUSTAFAR: RFG + lessstone ---
        if (type == PlanetType.MUSTAFAR) {

            // 1) Repulsor Field Generator – rudy w lawie
            if (MUSTAFAR_ORES.contains(mat)) {
                handleRfgForOre(event, player, block);
                if (event.isCancelled()) {
                    return; // RFG zablokował wydobycie
                }
            }

            // 2) Less stone – istniejąca logika
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

    private void handleRfgForOre(BlockBreakEvent event, Player player, Block block) {
        World world = block.getWorld();
        String worldName = world.getName();

        // Czy RFG jest włączony?
        boolean enabled = plugin.getConfigManager()
                .getBooleanForWorld(worldName, "rules.rfg.enable", true);
        if (!enabled) return;

        // Czy ruda jest "w lawie" (sąsiaduje z lawą)?
        if (!isOreInLavaField(block)) return;

        // Konfiguracja
        int coalCost = plugin.getConfigManager()
                .getIntForWorld(worldName, "rules.rfg.coal", 5);
        int redstoneCost = plugin.getConfigManager()
                .getIntForWorld(worldName, "rules.rfg.redstone", 10);
        String rfgName = plugin.getConfigManager()
                .getStringForWorld(worldName, "rules.rfg.name", "Repulsor Field Generator");

        String msg1 = color(plugin.getConfigManager()
                .getStringForWorld(worldName, "rules.rfg.message1",
                        "&cPotrzebujesz Repulsor Field Generator aby wydobyć rudę z lawy."));
        String msg2 = color(plugin.getConfigManager()
                .getStringForWorld(worldName, "rules.rfg.message2",
                        "&cPotrzebujesz Redstone aby zasilić Repulsor Field Generator."));
        String msg3 = color(plugin.getConfigManager()
                .getStringForWorld(worldName, "rules.rfg.message3",
                        "&cPotrzebujesz Węgiel aby zasilić Repulsor Field Generator."));
        String msg4 = color(plugin.getConfigManager()
                .getStringForWorld(worldName, "rules.rfg.message4",
                        "&cBrakuje miejsca w ekwipunku."));

        // 1) Czy gracz ma RFG w ręce?
        if (!hasRfgInHand(player, rfgName)) {
            player.sendMessage(msg1);
            event.setCancelled(true);
            return;
        }

        PlayerInventory inv = player.getInventory();

        // 2) Czy ma wystarczająco redstone?
        if (!hasMaterial(inv, Material.REDSTONE, redstoneCost)) {
            player.sendMessage(msg2);
            event.setCancelled(true);
            return;
        }

        // 3) Czy ma wystarczająco węgla?
        if (!hasMaterial(inv, Material.COAL, coalCost)) {
            player.sendMessage(msg3);
            event.setCancelled(true);
            return;
        }

        // 4) Czy ma miejsce w eq (opcjonalne, jeśli chcesz to egzekwować)?
        if (inv.firstEmpty() == -1) {
            player.sendMessage(msg4);
            event.setCancelled(true);
            return;
        }

        // 5) Zużyj surowce i pozwól na drop
        removeMaterial(inv, Material.REDSTONE, redstoneCost);
        removeMaterial(inv, Material.COAL, coalCost);
        // event NIE jest anulowany – blok się normalnie wykopie
    }

    private boolean isOreInLavaField(Block block) {
        // Sprawdzamy 6 sąsiadów – jeśli którykolwiek to lawa, traktujemy rudę jako "w lawie"
        Block[] neighbors = new Block[] {
                block.getRelative(1, 0, 0),
                block.getRelative(-1, 0, 0),
                block.getRelative(0, 1, 0),
                block.getRelative(0, -1, 0),
                block.getRelative(0, 0, 1),
                block.getRelative(0, 0, -1)
        };

        for (Block b : neighbors) {
            Material m = b.getType();
            if (m == Material.LAVA) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRfgInHand(Player player, String rfgName) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) {
            return false;
        }
        String display = ChatColor.stripColor(hand.getItemMeta().getDisplayName());
        String target  = ChatColor.stripColor(color(rfgName));
        return display.equalsIgnoreCase(target);
    }

    private boolean hasMaterial(PlayerInventory inv, Material mat, int amount) {
        int count = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == mat) {
                count += stack.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeMaterial(PlayerInventory inv, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != mat) continue;

            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;

            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            if (remaining <= 0) break;
        }
        inv.setContents(contents);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private void dropOne(World world, Block block, Material mat) {
        world.dropItemNaturally(block.getLocation(), new ItemStack(mat, 1));
    }
}
