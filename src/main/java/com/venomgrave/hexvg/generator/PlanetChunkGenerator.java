package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.populator.hoth.HothPopulator;
import com.venomgrave.hexvg.populator.tatooine.TatooinePopulator;
import com.venomgrave.hexvg.populator.dagobah.DagobahPopulator;
import com.venomgrave.hexvg.populator.dagobah.MushroomHutPopulator;
import com.venomgrave.hexvg.populator.dagobah.SpiderForestPopulator;
import com.venomgrave.hexvg.populator.dagobah.SwampTemplePopulator;
import com.venomgrave.hexvg.populator.mustafar.MustafarPopulator;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class PlanetChunkGenerator extends ChunkGenerator {

    private final String worldName;
    private final PlanetType type;


    private TerrainGenerator terrainGenerator;

    public PlanetChunkGenerator(String worldName, PlanetType type) {
        this.worldName = worldName;
        this.type = type;
    }


    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
        ensureTerrainGenerator(worldInfo);
        terrainGenerator.generate(worldInfo, random, chunkX, chunkZ, chunk);
    }


    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
    }


    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunk) {
    }


    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new PlanetBiomeProvider(type);
    }


    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        List<BlockPopulator> list = new ArrayList<>();
        switch (type) {
            case HOTH:
                list.add(new HothPopulator());
                break;
            case TATOOINE:
                list.add(new TatooinePopulator());
                break;
            case DAGOBAH:
                list.add(new DagobahPopulator());
                list.add(new MushroomHutPopulator());
                list.add(new SpiderForestPopulator());
                list.add(new SwampTemplePopulator());
                break;
            case MUSTAFAR:
                list.add(new MustafarPopulator());
                break;
        }
        return list;
    }

    @Override
    public boolean shouldGenerateNoise() { return false; }

    @Override
    public boolean shouldGenerateSurface() { return false; }

    @Override
    public boolean shouldGenerateBedrock() { return false; }

    @Override
    public boolean shouldGenerateCaves() { return false; }

    @Override
    public boolean shouldGenerateDecorations() { return false; }

    @Override
    public boolean shouldGenerateMobs() { return false; }

    @Override
    public boolean shouldGenerateStructures() { return false; }



    public PlanetType getPlanetType() { return type; }
    public String getWorldName()      { return worldName; }



    private void ensureTerrainGenerator(WorldInfo worldInfo) {
        if (terrainGenerator != null) return;
        HexVGPlanets plugin = HexVGPlanets.getInstance();
        switch (type) {
            case TATOOINE:
                terrainGenerator = new TatooineTerrainGenerator(plugin, worldInfo);
                break;
            case DAGOBAH:
                terrainGenerator = new DagobahTerrainGenerator(plugin, worldInfo);
                break;
            case MUSTAFAR:
                terrainGenerator = new MustafarTerrainGenerator(plugin, worldInfo);
                break;
            case HOTH:
            default:
                terrainGenerator = new HothTerrainGenerator(plugin, worldInfo);
                break;
        }

        if (!plugin.isPlanetWorld(worldName)) {
            plugin.getWorldConfigManager().registerWorld(worldName, type);
        }
    }
}