package com.venomgrave.hexvg.recipe;

import com.venomgrave.hexvg.HexVGPlanets;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class RecipeManager {

    private RecipeManager() {}

    public static ItemStack makeSuitItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta  = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName("§6" + displayName);
        String tag = getSuitTag(displayName);
        meta.setLore(java.util.Arrays.asList(
                tag,
                "§7HexVG-Planets protection suit"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private static String getSuitTag(String displayName) {
        if (displayName.startsWith(WARM_SUIT_TAG))      return WARM_SUIT_TAG;
        if (displayName.startsWith(COOLING_SUIT_TAG))   return COOLING_SUIT_TAG;
        if (displayName.startsWith(REPELLENT_SUIT_TAG)) return REPELLENT_SUIT_TAG;
        if (displayName.startsWith(GLASS_SUIT_TAG))     return GLASS_SUIT_TAG;
        return displayName;
    }


    public static final String WARM_SUIT_TAG      = "Warm Suit";
    public static final String COOLING_SUIT_TAG   = "Cooling Suit";
    public static final String REPELLENT_SUIT_TAG = "Repellent Suit";
    public static final String GLASS_SUIT_TAG     = "Glass Suit";

    public static void registerAll(HexVGPlanets plugin) {
        registerWarmSuit(plugin);
        registerRepellentSuit(plugin);
        registerCoolingSuit(plugin);
        registerGlassSuit(plugin);
    }

    private static void registerWarmSuit(HexVGPlanets plugin) {
        String displayName = plugin.getConfigManager().getString("recipe.warm.suit.name", "Warm Suit");

        registerArmour(plugin, "warm_helmet",
                Material.LEATHER_HELMET,     displayName + " Helmet",
                new String[]{"WWW","WNW","WWW"},
                'W', Material.WHITE_WOOL, 'N', Material.STRING);

        registerArmour(plugin, "warm_chestplate",
                Material.LEATHER_CHESTPLATE, displayName + " Chestplate",
                new String[]{"WWW","WNW","WWW"},
                'W', Material.WHITE_WOOL, 'N', Material.STRING);

        registerArmour(plugin, "warm_leggings",
                Material.LEATHER_LEGGINGS,   displayName + " Leggings",
                new String[]{"WWW","WNW","WWW"},
                'W', Material.WHITE_WOOL, 'N', Material.STRING);

        registerArmour(plugin, "warm_boots",
                Material.LEATHER_BOOTS,      displayName + " Boots",
                new String[]{"WWW","WNW","WWW"},
                'W', Material.WHITE_WOOL, 'N', Material.STRING);
    }

    private static void registerRepellentSuit(HexVGPlanets plugin) {
        String displayName = plugin.getConfigManager().getString("recipe.repellent.suit.name", "Repellent Suit");

        registerArmour(plugin, "repellent_helmet",
                Material.LEATHER_HELMET,     displayName + " Helmet",
                new String[]{"SSS","SNS","SSS"},
                'S', Material.SLIME_BALL, 'N', Material.STRING);

        registerArmour(plugin, "repellent_chestplate",
                Material.LEATHER_CHESTPLATE, displayName + " Chestplate",
                new String[]{"SSS","SNS","SSS"},
                'S', Material.SLIME_BALL, 'N', Material.STRING);

        registerArmour(plugin, "repellent_leggings",
                Material.LEATHER_LEGGINGS,   displayName + " Leggings",
                new String[]{"SSS","SNS","SSS"},
                'S', Material.SLIME_BALL, 'N', Material.STRING);

        registerArmour(plugin, "repellent_boots",
                Material.LEATHER_BOOTS,      displayName + " Boots",
                new String[]{"SSS","SNS","SSS"},
                'S', Material.SLIME_BALL, 'N', Material.STRING);
    }

    private static void registerCoolingSuit(HexVGPlanets plugin) {
        String displayName = plugin.getConfigManager().getString("recipe.cooling.suit.name", "Cooling Suit");

        registerArmour(plugin, "cooling_helmet",
                Material.LEATHER_HELMET,     displayName + " Helmet",
                new String[]{"CCC","CNC","CCC"},
                'C', Material.CLAY_BALL, 'N', Material.STRING);

        registerArmour(plugin, "cooling_chestplate",
                Material.LEATHER_CHESTPLATE, displayName + " Chestplate",
                new String[]{"CCC","CNC","CCC"},
                'C', Material.CLAY_BALL, 'N', Material.STRING);

        registerArmour(plugin, "cooling_leggings",
                Material.LEATHER_LEGGINGS,   displayName + " Leggings",
                new String[]{"CCC","CNC","CCC"},
                'C', Material.CLAY_BALL, 'N', Material.STRING);

        registerArmour(plugin, "cooling_boots",
                Material.LEATHER_BOOTS,      displayName + " Boots",
                new String[]{"CCC","CNC","CCC"},
                'C', Material.CLAY_BALL, 'N', Material.STRING);
    }

    private static void registerGlassSuit(HexVGPlanets plugin) {
        String displayName = plugin.getConfigManager().getString("recipe.glass.suit.name", "Glass Suit");

        registerArmour(plugin, "glass_helmet",
                Material.LEATHER_HELMET,     displayName + " Helmet",
                new String[]{"GGG","GNG","GGG"},
                'G', Material.GLASS_PANE, 'N', Material.STRING);

        registerArmour(plugin, "glass_chestplate",
                Material.LEATHER_CHESTPLATE, displayName + " Chestplate",
                new String[]{"GGG","GNG","GGG"},
                'G', Material.GLASS_PANE, 'N', Material.STRING);

        registerArmour(plugin, "glass_leggings",
                Material.LEATHER_LEGGINGS,   displayName + " Leggings",
                new String[]{"GGG","GNG","GGG"},
                'G', Material.GLASS_PANE, 'N', Material.STRING);

        registerArmour(plugin, "glass_boots",
                Material.LEATHER_BOOTS,      displayName + " Boots",
                new String[]{"GGG","GNG","GGG"},
                'G', Material.GLASS_PANE, 'N', Material.STRING);
    }


    private static void registerArmour(HexVGPlanets plugin, String keyName, Material result, String displayName, String[] shape, char ingredient,  Material ingredientMat, char leather,     Material leatherMat) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);

        plugin.getServer().removeRecipe(key);

        ItemStack item = makeSuitItem(result, displayName);
        if (item == null) item = new ItemStack(result);

        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(shape);
        recipe.setIngredient(ingredient, ingredientMat);
        recipe.setIngredient(leather,    leatherMat);

        try {
            plugin.getServer().addRecipe(recipe);
        } catch (Exception e) {
            plugin.getLogger().warning("Could not register recipe " + keyName + ": " + e.getMessage());
        }
    }
}