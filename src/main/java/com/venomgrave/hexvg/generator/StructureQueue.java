package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.config.ConfigManager;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;
import com.venomgrave.hexvg.task.SchematicPlaceTask;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;


public final class StructureQueue {

    private StructureQueue() {}



    private static final int DEF_BASE        = 200;
    private static final int DEF_DOME        = 350;
    private static final int DEF_ROOM        = 150;
    private static final int DEF_SARLACC     = 400;
    private static final int DEF_HUT         = 180;
    private static final int DEF_SANDCASTLE  = 250;
    private static final int DEF_SKELETON    = 300;
    private static final int DEF_TEMPLE      = 500;
    private static final int DEF_SPIDERS     = 300;
    private static final int DEF_MUSHROOM    = 200;
    private static final int DEF_TREEHUT     = 200;
    private static final int DEF_MUS_BASE    = 400;
    private static final int DEF_MUS_TEMPLE  = 500;
    private static final int DEF_LAVAFOUNTAIN= 150;


    public static void queue(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, PlanetType type) {HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        long chunkSeed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ worldInfo.getSeed();


        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld(worldInfo.getName());
            if (world == null) return;

            String wn = world.getName();
            Random rng = new Random(chunkSeed);


            if (ConfigManager.isGenerateOres(plugin, world)) {
                OreGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0xABCDL));
            }


            boolean caves = plugin.getConfigManager()
                    .getBooleanForWorld(wn, "rules.caves", true);
            if (caves) {
                CaveGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0x1234L));
            }


            if (type != PlanetType.DAGOBAH) {
                boolean spikes = plugin.getConfigManager()
                        .getBooleanForWorld(wn, "rules.spikes", true);
                if (spikes) {
                    SpikeGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0x5678L));
                }
            }


            queueSchematics(plugin, world, wn, chunkX, chunkZ, type, rng);

        }, 2L);
    }


    private static void queueSchematics(HexVGPlanets plugin, World world, String wn, int chunkX, int chunkZ, PlanetType type, Random rng) {
        switch (type) {

            case HOTH:
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "hoth_base",  "bases",   DEF_BASE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "hoth_dome",  "domes",   DEF_DOME);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "hoth_room",  "bases",   DEF_ROOM);
                break;

            case TATOOINE:
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "tatooine_sarlacc",   "sarlacc",    DEF_SARLACC);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "tatooine_hut",       "village",    DEF_HUT);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "tatooine_base",      "bases",      DEF_BASE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "tatooine_sandcastle","sandcastle", DEF_SANDCASTLE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "tatooine_skeleton",  "skeletons",  DEF_SKELETON);
                break;

            case DAGOBAH:
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "dagobah_temple",    "swamptemple",  DEF_TEMPLE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "dagobah_hut",       "treehut",      DEF_TREEHUT);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "dagobah_spiders",   "spiderforest", DEF_SPIDERS);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "dagobah_mushroomhut","mushroomhuts",DEF_MUSHROOM);
                break;

            case MUSTAFAR:
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "mustafar_base",          "mustafarbase",   DEF_MUS_BASE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "mustafar_temple",        "mustafartemple", DEF_MUS_TEMPLE);
                trySchematic(plugin, world, wn, chunkX, chunkZ, rng,
                        "mustafar_lava_fountain", "bases",          DEF_LAVAFOUNTAIN);
                break;
        }
    }


    private static void trySchematic(HexVGPlanets plugin, World world, String worldName, int chunkX, int chunkZ, Random rng, String schematicName, String configKey, int defaultRarity) {

        int rarity = plugin.getConfigManager()
                .getIntForWorld(worldName, "structure." + configKey + ".rarity", defaultRarity);

        if (rarity <= 0) return;
        if (rng.nextInt(rarity) != 0) return;

        Optional<Schematic> opt = plugin.getSchematicRegistry().get(schematicName);
        if (!opt.isPresent()) return;

        int sx  = chunkX * 16 + rng.nextInt(16);
        int sz  = chunkZ * 16 + rng.nextInt(16);
        int sy  = world.getHighestBlockYAt(sx, sz);
        int dir = rng.nextInt(4);

        Schematic rotated = SchematicRotator.rotate(opt.get(), dir);

        plugin.getTaskQueue().add(
                new SchematicPlaceTask(plugin, world, rotated, sx, sy, sz, dir));

        if (ConfigManager.isDebug(plugin)) {
            plugin.getLogger().info("[StructureQueue] " + schematicName
                    + " @ " + sx + "," + sy + "," + sz
                    + " (rarity=1/" + rarity + ", world=" + worldName + ")");
        }
    }
}