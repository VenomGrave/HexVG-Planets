package com.venomgrave.hexvg.schematic;

import com.venomgrave.hexvg.loot.LootGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public final class SchematicPlacer {

    private SchematicPlacer() {}


    private static final Set<Material> DEFERRED = EnumSet.of(
            Material.TORCH,              Material.WALL_TORCH,
            Material.REDSTONE_TORCH,     Material.REDSTONE_WALL_TORCH,
            Material.SOUL_TORCH,         Material.SOUL_WALL_TORCH,
            Material.OAK_SAPLING,        Material.SPRUCE_SAPLING,
            Material.BIRCH_SAPLING,      Material.JUNGLE_SAPLING,
            Material.ACACIA_SAPLING,     Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE, Material.CHERRY_SAPLING,
            Material.POPPY,              Material.DANDELION,
            Material.BLUE_ORCHID,        Material.ALLIUM,
            Material.AZURE_BLUET,        Material.RED_TULIP,
            Material.ORANGE_TULIP,       Material.WHITE_TULIP,
            Material.PINK_TULIP,         Material.OXEYE_DAISY,
            Material.CORNFLOWER,         Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER,          Material.LILAC,
            Material.ROSE_BUSH,          Material.PEONY,
            Material.TALL_GRASS,         Material.LARGE_FERN,
            Material.WHEAT,              Material.CARROTS,
            Material.POTATOES,           Material.BEETROOTS,
            Material.SUGAR_CANE,         Material.CACTUS,
            Material.BAMBOO,             Material.BAMBOO_SAPLING,
            Material.VINE,               Material.LILY_PAD,
            Material.OAK_DOOR,           Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,         Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,        Material.DARK_OAK_DOOR,
            Material.MANGROVE_DOOR,      Material.CHERRY_DOOR,
            Material.IRON_DOOR,
            Material.RAIL,               Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,      Material.ACTIVATOR_RAIL,
            Material.LEVER,
            Material.STONE_BUTTON,       Material.OAK_BUTTON,
            Material.SPRUCE_BUTTON,      Material.BIRCH_BUTTON,
            Material.JUNGLE_BUTTON,      Material.ACACIA_BUTTON,
            Material.DARK_OAK_BUTTON,    Material.POLISHED_BLACKSTONE_BUTTON,
            Material.LADDER,
            Material.OAK_SIGN,           Material.SPRUCE_SIGN,
            Material.BIRCH_SIGN,         Material.JUNGLE_SIGN,
            Material.ACACIA_SIGN,        Material.DARK_OAK_SIGN,
            Material.OAK_WALL_SIGN,      Material.SPRUCE_WALL_SIGN,
            Material.BIRCH_WALL_SIGN,    Material.JUNGLE_WALL_SIGN,
            Material.ACACIA_WALL_SIGN,   Material.DARK_OAK_WALL_SIGN,
            Material.TRIPWIRE,           Material.TRIPWIRE_HOOK,
            Material.REPEATER,           Material.COMPARATOR,
            Material.REDSTONE_WIRE,
            Material.SNOW,               Material.SEAGRASS,
            Material.TALL_SEAGRASS,      Material.KELP,
            Material.KELP_PLANT,
            Material.HANGING_ROOTS,      Material.MOSS_CARPET
    );

    private static final Set<Material> LOOT_CONTAINERS = EnumSet.of(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.BARREL,
            Material.WHITE_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.SHULKER_BOX
    );

    public static void place(World world, Schematic schematic, int originX, int originY, int originZ, LootGenerator loot) {
        if (world == null || schematic == null) return;

        int height = schematic.getHeight();
        int length = schematic.getLength();
        int width  = schematic.getWidth();

        List<int[]>          deferredCoords = new ArrayList<>();
        List<SchematicBlock> deferredBlocks = new ArrayList<>();

        for (int yy = 0; yy < height; yy++) {
            for (int zz = 0; zz < length; zz++) {
                for (int xx = 0; xx < width; xx++) {
                    SchematicBlock sb = schematic.getBlock(yy, zz, xx);
                    if (sb == null || sb.isSkip()) continue;

                    int wx = originX + xx;
                    int wy = originY - yy;   // Y spada od góry w dół
                    int wz = originZ + zz;

                    if (outOfWorld(world, wx, wy, wz)) continue;

                    if (sb.isEntitySpawn()) {
                        spawnEntity(world, wx, wy, wz, sb.getEntityTypeName());
                        continue;
                    }

                    Material mat = sb.getMaterial();
                    if (mat == null) continue;

                    if (DEFERRED.contains(mat)) {
                        deferredCoords.add(new int[]{wx, wy, wz});
                        deferredBlocks.add(sb);
                        continue;
                    }

                    placeBlockInWorld(world, wx, wy, wz, sb, loot);
                }
            }
        }

        for (int i = 0; i < deferredCoords.size(); i++) {
            int[] c = deferredCoords.get(i);
            placeBlockInWorld(world, c[0], c[1], c[2], deferredBlocks.get(i), loot);
        }
    }

    public static void placeInRegion(LimitedRegion region, Schematic schematic, int originX, int originY, int originZ, LootGenerator lootGen) {
        if (region == null || schematic == null) return;

        int height = schematic.getHeight();
        int length = schematic.getLength();
        int width  = schematic.getWidth();

        List<int[]>          deferredCoords = new ArrayList<>();
        List<SchematicBlock> deferredBlocks = new ArrayList<>();

        for (int yy = 0; yy < height; yy++) {
            for (int zz = 0; zz < length; zz++) {
                for (int xx = 0; xx < width; xx++) {
                    SchematicBlock sb = schematic.getBlock(yy, zz, xx);
                    if (sb == null || sb.isSkip() || sb.isEntitySpawn()) continue;

                    Material mat = sb.getMaterial();
                    if (mat == null) continue;

                    int bx = originX + xx;
                    int by = originY - yy;
                    int bz = originZ + zz;

                    if (!region.isInRegion(bx, by, bz)) continue;

                    if (DEFERRED.contains(mat)) {
                        deferredCoords.add(new int[]{bx, by, bz});
                        deferredBlocks.add(sb);
                        continue;
                    }

                    placeBlockInRegion(region, bx, by, bz, mat);
                }
            }
        }

        for (int i = 0; i < deferredCoords.size(); i++) {
            int[] c = deferredCoords.get(i);
            if (region.isInRegion(c[0], c[1], c[2])) {
                placeBlockInRegion(region, c[0], c[1], c[2],
                        deferredBlocks.get(i).getMaterial());
            }
        }
    }


    private static void placeBlockInWorld(World world, int x, int y, int z, SchematicBlock sb, LootGenerator loot) {
        Block block = world.getBlockAt(x, y, z);
        Material mat = sb.getMaterial();

        if (mat == Material.AIR) {
            block.setType(Material.AIR, false);
            return;
        }

        if (mat == Material.SPAWNER) {
            block.setType(Material.SPAWNER, false);
            if (block.getState() instanceof CreatureSpawner) {
                CreatureSpawner cs = (CreatureSpawner) block.getState();
                EntityType et = parseEntityType(sb.getSpawnerEntityType());
                if (et != null) cs.setSpawnedType(et);
                cs.update(true, false);
            }
            return;
        }

        if (LOOT_CONTAINERS.contains(mat)) {
            block.setType(mat, false);
            applyBlockData(block, sb.getBlockDataString());
            if (loot != null && block.getState() instanceof Container) {
                Container container = (Container) block.getState();
                Inventory inv = container.getInventory();
                loot.fill(inv);
                container.update(true, false);
            }
            return;
        }

        block.setType(mat, false);
        applyBlockData(block, sb.getBlockDataString());
    }

    private static void placeBlockInRegion(LimitedRegion region,
                                           int x, int y, int z, Material mat) {
        if (mat == null || mat == Material.AIR) return;
        try {
            region.setType(x, y, z, mat);
        } catch (Exception ignored) {
            // Ignoruj błędy poza granicami lub nieobsługiwane materiały
        }
    }


    private static void applyBlockData(Block block, String bdString) {
        if (bdString == null || bdString.isEmpty()) return;
        try {
            BlockData bd = Bukkit.createBlockData(bdString);
            block.setBlockData(bd, false);
        } catch (Exception ignored) {
        }
    }

    private static void spawnEntity(World world, int x, int y, int z, String typeName) {
        EntityType et = parseEntityType(typeName);
        if (et == null) return;
        try {
            world.spawnEntity(new Location(world, x + 0.5, y, z + 0.5), et);
        } catch (Exception ignored) {}
    }

    private static EntityType parseEntityType(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean outOfWorld(World world, int x, int y, int z) {
        return y < world.getMinHeight() || y >= world.getMaxHeight();
    }
}