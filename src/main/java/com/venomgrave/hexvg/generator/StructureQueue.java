package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.task.SchematicPlaceTask;
import com.venomgrave.hexvg.world.PlanetType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.generator.WorldInfo;

import java.util.Optional;
import java.util.Random;

public final class StructureQueue {

    private StructureQueue() {}

    private static final int DEF = 200;

    public static void queue(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, PlanetType type) {
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        if (plugin == null) return;

        long chunkSeed = (long) chunkX * 341873128712L
                ^ (long) chunkZ * 132897987541L
                ^ worldInfo.getSeed();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World world = Bukkit.getWorld(worldInfo.getName());
            if (world == null) return;

            String wn = world.getName();
            Random rng = new Random(chunkSeed);

            if (plugin.getConfigManager().getBooleanForWorld(wn, "generate.ores", true)) {
                OreGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0xABCDL));
            }

            int caveRarity = plugin.getConfigManager().getIntForWorld(wn, "generate.caves.rarity", 1);
            if (caveRarity > 0 && rng.nextInt(caveRarity) == 0) {
                CaveGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0x1234L));
            }

            if (type != PlanetType.DAGOBAH) {
                if (plugin.getConfigManager().getBooleanForWorld(wn, "generate.spikes", true)) {
                    SpikeGenerator.generate(world, chunkX, chunkZ, type, new Random(chunkSeed ^ 0x5678L));
                }
            }

            queueSchematics(plugin, world, wn, chunkX, chunkZ, type, rng);

        }, 2L);
    }

    private static void queueSchematics(HexVGPlanets plugin, World world, String wn,
                                        int chunkX, int chunkZ, PlanetType type, Random rng) {

        switch (type) {
            case HOTH:
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "domes", "hoth_dome");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "bases", "hoth_base");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "bases", "hoth_room");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "spikes", "hoth_spike");
                break;

            case TATOOINE:
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "sarlacc", "tatooine_sarlacc");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "village", "tatooine_hut");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "bases", "tatooine_base");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "sandcastle", "tatooine_sandcastle");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "skeletons", "tatooine_skeleton");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "oasis", "tatooine_oasis");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "gardens", "tatooine_garden");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "supergarden", "tatooine_supergarden");
                break;

            case DAGOBAH:
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "swamptemple", "dagobah_temple");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "treehut", "dagobah_hut");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "spiderforest", "dagobah_spiders");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "mushroomhuts", "dagobah_mushroomhut");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "hugetree", "dagobah_hugetree");
                break;

            case MUSTAFAR:
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "mustafarbase", "mustafar_base");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "mustafartemple", "mustafar_temple");
                tryStructure(plugin, world, wn, chunkX, chunkZ, rng, "bases", "mustafar_lava_fountain");
                break;
        }
    }

    private static void tryStructure(HexVGPlanets plugin, World world, String wn,
                                     int chunkX, int chunkZ, Random rng,
                                     String configKey, String schematicName) {

        if (!plugin.getConfigManager().getBooleanForWorld(wn, "generate." + configKey, true))
            return;

        int rarity = plugin.getConfigManager()
                .getIntForWorld(wn, "structure." + configKey + ".rarity", DEF);

        if (rarity <= 0) return;
        if (rng.nextInt(rarity) != 0) return;

        Optional<Schematic> opt = plugin.getSchematicRegistry().get(schematicName);
        if (!opt.isPresent()) return;

        int sx = chunkX * 16 + rng.nextInt(16);
        int sz = chunkZ * 16 + rng.nextInt(16);

        int groundY = world.getHighestBlockYAt(sx, sz);
        int sy = groundY + 1; // stawiamy strukturę na powierzchni

        int dir = rng.nextInt(4);

        plugin.getTaskQueue().add(
                new SchematicPlaceTask(plugin, world, opt.get(), sx, sy, sz, dir,
                        plugin.getLootGenerator())
        );
    }
}
