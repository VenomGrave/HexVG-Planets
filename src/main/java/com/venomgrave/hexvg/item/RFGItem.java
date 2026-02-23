package com.venomgrave.hexvg.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class RFGItem {

    public static final String RFG_TAG = "RepulsorFieldGenerator";

    public static ItemStack create(String displayName) {
        ItemStack item = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Repulsor Field Generator",
                ChatColor.DARK_GRAY + "(Pozwala wydobywać rudy z lawy)"
        ));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }
}
