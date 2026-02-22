package com.venomgrave.hexvg.schematic;

import org.bukkit.Material;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class SchematicConverter {

    private SchematicConverter() {}

    private static final Map<Integer, Material> LEGACY_MAP = new HashMap<>();

    static {

        LEGACY_MAP.put(0,   Material.AIR);
        LEGACY_MAP.put(1,   Material.STONE);
        LEGACY_MAP.put(2,   Material.GRASS_BLOCK);
        LEGACY_MAP.put(3,   Material.DIRT);
        LEGACY_MAP.put(4,   Material.COBBLESTONE);
        LEGACY_MAP.put(5,   Material.OAK_PLANKS);
        LEGACY_MAP.put(7,   Material.BEDROCK);
        LEGACY_MAP.put(8,   Material.WATER);
        LEGACY_MAP.put(9,   Material.WATER);
        LEGACY_MAP.put(10,  Material.LAVA);
        LEGACY_MAP.put(11,  Material.LAVA);
        LEGACY_MAP.put(12,  Material.SAND);
        LEGACY_MAP.put(13,  Material.GRAVEL);
        LEGACY_MAP.put(14,  Material.GOLD_ORE);
        LEGACY_MAP.put(15,  Material.IRON_ORE);
        LEGACY_MAP.put(16,  Material.COAL_ORE);
        LEGACY_MAP.put(17,  Material.OAK_LOG);
        LEGACY_MAP.put(18,  Material.OAK_LEAVES);
        LEGACY_MAP.put(20,  Material.GLASS);
        LEGACY_MAP.put(21,  Material.LAPIS_ORE);
        LEGACY_MAP.put(22,  Material.LAPIS_BLOCK);
        LEGACY_MAP.put(24,  Material.SANDSTONE);
        LEGACY_MAP.put(25,  Material.NOTE_BLOCK);
        LEGACY_MAP.put(35,  Material.WHITE_WOOL);
        LEGACY_MAP.put(41,  Material.GOLD_BLOCK);
        LEGACY_MAP.put(42,  Material.IRON_BLOCK);
        LEGACY_MAP.put(43,  Material.SMOOTH_STONE_SLAB);
        LEGACY_MAP.put(44,  Material.SMOOTH_STONE_SLAB);
        LEGACY_MAP.put(45,  Material.BRICKS);
        LEGACY_MAP.put(46,  Material.TNT);
        LEGACY_MAP.put(47,  Material.BOOKSHELF);
        LEGACY_MAP.put(48,  Material.MOSSY_COBBLESTONE);
        LEGACY_MAP.put(49,  Material.OBSIDIAN);
        LEGACY_MAP.put(50,  Material.TORCH);
        LEGACY_MAP.put(51,  Material.FIRE);
        LEGACY_MAP.put(52,  Material.SPAWNER);
        LEGACY_MAP.put(53,  Material.OAK_STAIRS);
        LEGACY_MAP.put(54,  Material.CHEST);
        LEGACY_MAP.put(56,  Material.DIAMOND_ORE);
        LEGACY_MAP.put(57,  Material.DIAMOND_BLOCK);
        LEGACY_MAP.put(58,  Material.CRAFTING_TABLE);
        LEGACY_MAP.put(60,  Material.FARMLAND);
        LEGACY_MAP.put(61,  Material.FURNACE);
        LEGACY_MAP.put(65,  Material.LADDER);
        LEGACY_MAP.put(66,  Material.RAIL);
        LEGACY_MAP.put(67,  Material.COBBLESTONE_STAIRS);
        LEGACY_MAP.put(73,  Material.REDSTONE_ORE);
        LEGACY_MAP.put(78,  Material.SNOW);
        LEGACY_MAP.put(79,  Material.ICE);
        LEGACY_MAP.put(80,  Material.SNOW_BLOCK);
        LEGACY_MAP.put(81,  Material.CACTUS);
        LEGACY_MAP.put(82,  Material.CLAY);
        LEGACY_MAP.put(83,  Material.SUGAR_CANE);
        LEGACY_MAP.put(85,  Material.OAK_FENCE);
        LEGACY_MAP.put(86,  Material.CARVED_PUMPKIN);
        LEGACY_MAP.put(87,  Material.NETHERRACK);
        LEGACY_MAP.put(88,  Material.SOUL_SAND);
        LEGACY_MAP.put(89,  Material.GLOWSTONE);
        LEGACY_MAP.put(91,  Material.JACK_O_LANTERN);
        LEGACY_MAP.put(95,  Material.WHITE_STAINED_GLASS);
        LEGACY_MAP.put(97,  Material.INFESTED_STONE);
        LEGACY_MAP.put(98,  Material.STONE_BRICKS);
        LEGACY_MAP.put(108, Material.BRICK_STAIRS);
        LEGACY_MAP.put(109, Material.STONE_BRICK_STAIRS);
        LEGACY_MAP.put(114, Material.NETHER_BRICK_STAIRS);
        LEGACY_MAP.put(121, Material.END_STONE);
        LEGACY_MAP.put(129, Material.EMERALD_ORE);
        LEGACY_MAP.put(133, Material.EMERALD_BLOCK);
        LEGACY_MAP.put(152, Material.REDSTONE_BLOCK);
        LEGACY_MAP.put(155, Material.QUARTZ_BLOCK);
        LEGACY_MAP.put(156, Material.QUARTZ_STAIRS);
        LEGACY_MAP.put(159, Material.WHITE_TERRACOTTA);
        LEGACY_MAP.put(161, Material.ACACIA_LEAVES);
        LEGACY_MAP.put(162, Material.ACACIA_LOG);
        LEGACY_MAP.put(163, Material.ACACIA_STAIRS);
        LEGACY_MAP.put(164, Material.DARK_OAK_STAIRS);
        LEGACY_MAP.put(170, Material.HAY_BLOCK);
        LEGACY_MAP.put(172, Material.TERRACOTTA);
        LEGACY_MAP.put(173, Material.COAL_BLOCK);
        LEGACY_MAP.put(174, Material.PACKED_ICE);
        LEGACY_MAP.put(179, Material.RED_SANDSTONE);
        LEGACY_MAP.put(214, Material.CRIMSON_NYLIUM);
        LEGACY_MAP.put(215, Material.RED_NETHER_BRICKS);
        LEGACY_MAP.put(6,   Material.OAK_SAPLING);
        LEGACY_MAP.put(26,  Material.RED_BED);
        LEGACY_MAP.put(27,  Material.POWERED_RAIL);
        LEGACY_MAP.put(28,  Material.DETECTOR_RAIL);
        LEGACY_MAP.put(29,  Material.STICKY_PISTON);
        LEGACY_MAP.put(30,  Material.COBWEB);
        LEGACY_MAP.put(31,  Material.TALL_GRASS);
        LEGACY_MAP.put(37,  Material.DANDELION);
        LEGACY_MAP.put(38,  Material.POPPY);
        LEGACY_MAP.put(39,  Material.BROWN_MUSHROOM);
        LEGACY_MAP.put(40,  Material.RED_MUSHROOM);
        LEGACY_MAP.put(59,  Material.WHEAT);
        LEGACY_MAP.put(69,  Material.LEVER);
        LEGACY_MAP.put(70,  Material.STONE_PRESSURE_PLATE);
        LEGACY_MAP.put(71,  Material.IRON_DOOR);
        LEGACY_MAP.put(72,  Material.OAK_PRESSURE_PLATE);
        LEGACY_MAP.put(75,  Material.REDSTONE_TORCH);
        LEGACY_MAP.put(76,  Material.REDSTONE_TORCH);
        LEGACY_MAP.put(77,  Material.STONE_BUTTON);
        LEGACY_MAP.put(84,  Material.JUKEBOX);
        LEGACY_MAP.put(90,  Material.NETHER_PORTAL);
        LEGACY_MAP.put(93,  Material.REPEATER);
        LEGACY_MAP.put(94,  Material.COMPARATOR);
        LEGACY_MAP.put(96,  Material.OAK_TRAPDOOR);
        LEGACY_MAP.put(99,  Material.BROWN_MUSHROOM_BLOCK);
        LEGACY_MAP.put(100, Material.RED_MUSHROOM_BLOCK);
        LEGACY_MAP.put(101, Material.IRON_BARS);
        LEGACY_MAP.put(102, Material.GLASS_PANE);
        LEGACY_MAP.put(103, Material.MELON);
        LEGACY_MAP.put(107, Material.OAK_FENCE_GATE);
        LEGACY_MAP.put(111, Material.LILY_PAD);
        LEGACY_MAP.put(112, Material.NETHER_BRICKS);
        LEGACY_MAP.put(113, Material.NETHER_BRICK_FENCE);
        LEGACY_MAP.put(116, Material.ENCHANTING_TABLE);
        LEGACY_MAP.put(117, Material.BREWING_STAND);
        LEGACY_MAP.put(118, Material.CAULDRON);
        LEGACY_MAP.put(120, Material.END_PORTAL_FRAME);
        LEGACY_MAP.put(122, Material.DRAGON_EGG);
        LEGACY_MAP.put(123, Material.REDSTONE_LAMP);
        LEGACY_MAP.put(125, Material.OAK_SLAB);
        LEGACY_MAP.put(126, Material.OAK_SLAB);
        LEGACY_MAP.put(130, Material.ENDER_CHEST);
        LEGACY_MAP.put(131, Material.TRIPWIRE_HOOK);
        LEGACY_MAP.put(132, Material.TRIPWIRE);
        LEGACY_MAP.put(137, Material.COMMAND_BLOCK);
        LEGACY_MAP.put(138, Material.BEACON);
        LEGACY_MAP.put(139, Material.COBBLESTONE_WALL);
        LEGACY_MAP.put(140, Material.FLOWER_POT);
        LEGACY_MAP.put(141, Material.CARROTS);
        LEGACY_MAP.put(142, Material.POTATOES);
        LEGACY_MAP.put(143, Material.OAK_BUTTON);
        LEGACY_MAP.put(144, Material.SKELETON_SKULL);
        LEGACY_MAP.put(145, Material.ANVIL);
        LEGACY_MAP.put(146, Material.TRAPPED_CHEST);
        LEGACY_MAP.put(147, Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        LEGACY_MAP.put(148, Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        LEGACY_MAP.put(149, Material.COMPARATOR);
        LEGACY_MAP.put(150, Material.COMPARATOR);
        LEGACY_MAP.put(151, Material.DAYLIGHT_DETECTOR);
        LEGACY_MAP.put(153, Material.NETHER_QUARTZ_ORE);
        LEGACY_MAP.put(154, Material.HOPPER);
        LEGACY_MAP.put(157, Material.ACTIVATOR_RAIL);
        LEGACY_MAP.put(158, Material.DROPPER);
        LEGACY_MAP.put(160, Material.WHITE_STAINED_GLASS_PANE);
        LEGACY_MAP.put(165, Material.SLIME_BLOCK);
        LEGACY_MAP.put(166, Material.BARRIER);
        LEGACY_MAP.put(167, Material.IRON_TRAPDOOR);
        LEGACY_MAP.put(168, Material.PRISMARINE);
        LEGACY_MAP.put(169, Material.SEA_LANTERN);
        LEGACY_MAP.put(171, Material.WHITE_CARPET);
        LEGACY_MAP.put(175, Material.SUNFLOWER);
        LEGACY_MAP.put(176, Material.WHITE_BANNER);
        LEGACY_MAP.put(177, Material.WHITE_WALL_BANNER);
        LEGACY_MAP.put(178, Material.DAYLIGHT_DETECTOR);
        LEGACY_MAP.put(180, Material.RED_SANDSTONE_STAIRS);
        LEGACY_MAP.put(181, Material.RED_SANDSTONE_SLAB);
        LEGACY_MAP.put(182, Material.RED_SANDSTONE_SLAB);
        LEGACY_MAP.put(183, Material.SPRUCE_FENCE_GATE);
        LEGACY_MAP.put(184, Material.BIRCH_FENCE_GATE);
        LEGACY_MAP.put(185, Material.JUNGLE_FENCE_GATE);
        LEGACY_MAP.put(186, Material.DARK_OAK_FENCE_GATE);
        LEGACY_MAP.put(187, Material.ACACIA_FENCE_GATE);
        LEGACY_MAP.put(188, Material.SPRUCE_FENCE);
        LEGACY_MAP.put(189, Material.BIRCH_FENCE);
        LEGACY_MAP.put(190, Material.JUNGLE_FENCE);
        LEGACY_MAP.put(191, Material.DARK_OAK_FENCE);
        LEGACY_MAP.put(192, Material.ACACIA_FENCE);
        LEGACY_MAP.put(193, Material.SPRUCE_DOOR);
        LEGACY_MAP.put(194, Material.BIRCH_DOOR);
        LEGACY_MAP.put(195, Material.JUNGLE_DOOR);
        LEGACY_MAP.put(196, Material.ACACIA_DOOR);
        LEGACY_MAP.put(197, Material.DARK_OAK_DOOR);
        LEGACY_MAP.put(198, Material.END_ROD);
        LEGACY_MAP.put(199, Material.CHORUS_PLANT);
        LEGACY_MAP.put(200, Material.CHORUS_FLOWER);
        LEGACY_MAP.put(201, Material.PURPUR_BLOCK);
        LEGACY_MAP.put(202, Material.PURPUR_PILLAR);
        LEGACY_MAP.put(203, Material.PURPUR_STAIRS);
        LEGACY_MAP.put(204, Material.PURPUR_SLAB);
        LEGACY_MAP.put(205, Material.PURPUR_SLAB);
        LEGACY_MAP.put(206, Material.END_STONE_BRICKS);
        LEGACY_MAP.put(207, Material.BEETROOTS);
        LEGACY_MAP.put(208, Material.DIRT_PATH);
        LEGACY_MAP.put(209, Material.END_GATEWAY);
        LEGACY_MAP.put(210, Material.REPEATING_COMMAND_BLOCK);
        LEGACY_MAP.put(211, Material.CHAIN_COMMAND_BLOCK);
        LEGACY_MAP.put(212, Material.FROSTED_ICE);
        LEGACY_MAP.put(213, Material.MAGMA_BLOCK);
        LEGACY_MAP.put(216, Material.BONE_BLOCK);
        LEGACY_MAP.put(218, Material.OBSERVER);
        LEGACY_MAP.put(219, Material.WHITE_SHULKER_BOX);
        LEGACY_MAP.put(255, Material.STRUCTURE_BLOCK);
        LEGACY_MAP.put(32,  Material.DEAD_BUSH);
        LEGACY_MAP.put(33,  Material.PISTON);
        LEGACY_MAP.put(55,  Material.REDSTONE_WIRE);
        LEGACY_MAP.put(64,  Material.OAK_DOOR);
        LEGACY_MAP.put(104, Material.PUMPKIN_STEM);
        LEGACY_MAP.put(106, Material.VINE);
        LEGACY_MAP.put(110, Material.MYCELIUM);
        LEGACY_MAP.put(128, Material.SANDSTONE_STAIRS);
    }

    public static void convertFile(File input, File output, String name, Logger log)
            throws IOException {
        String content = convertStream(
                new FileInputStream(input), name, log);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(output), StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }

    public static String convertStream(InputStream in, String name, Logger log) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String[] dim = reader.readLine().trim().split("\\s+");
        int width  = Integer.parseInt(dim[0]);
        int height = Integer.parseInt(dim[1]);
        int length = Integer.parseInt(dim[2]);

        String line2 = reader.readLine().trim();
        int lootMin = 2, lootMax = 10;
        try {
            String[] loot = line2.split("\\s+");
            lootMin = Integer.parseInt(loot[0]);
            lootMax = Integer.parseInt(loot[1]);
        } catch (Exception e) {
        }

        List<String> blockLines = new ArrayList<>();
        int unknownCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("\\s+");
            if (parts.length < 3) continue;

            int blockId = Integer.parseInt(parts[0]);
            int by = Integer.parseInt(parts[2]);
            int bz = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0;
            int bx = parts.length >= 5 ? Integer.parseInt(parts[4]) : 0;

            Material mat = LEGACY_MAP.get(blockId);
            if (mat == null) {
                if (log != null) {
                    log.warning("[SchematicConverter] Nieznany block ID " + blockId + " w schemacie '" + name + "', zastępuję STONE");
                }
                mat = Material.STONE;
                unknownCount++;
            }

            if (mat == Material.AIR) continue;

            if (blockId == 52) {
                blockLines.add(by + " " + bz + " " + bx + " SPAWNER ZOMBIE");
            } else {
                blockLines.add(by + " " + bz + " " + bx + " " + mat.name());
            }
        }
        reader.close();

        if (log != null && unknownCount > 0) {
            log.warning("[SchematicConverter] Schemat '" + name + "': " + unknownCount + " nieznanych bloków zastąpionych STONE.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Converted from HothGenerator 3.x legacy format\n");
        sb.append("name: ").append(name).append("\n");
        sb.append("size: ").append(width).append(" ").append(height).append(" ").append(length).append("\n");
        sb.append("loot: ").append(lootMin).append(" ").append(lootMax).append("\n");
        sb.append("blocks:\n");
        for (String bl : blockLines) {
            sb.append(bl).append("\n");
        }
        return sb.toString();
    }


    public static Material getLegacyMaterial(int blockId) {
        return LEGACY_MAP.get(blockId);
    }

    public static boolean isLegacyFormat(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String first = reader.readLine();
        reader.close();
        if (first == null) return false;
        first = first.trim();
        if (first.startsWith("name:") || first.startsWith("#") || first.startsWith("size:")) return false;
        String[] parts = first.split("\\s+");
        if (parts.length < 2) return false;
        try {
            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}