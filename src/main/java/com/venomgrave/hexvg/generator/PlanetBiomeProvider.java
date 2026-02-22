package com.venomgrave.hexvg.generator;

import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

import java.util.Collections;
import java.util.List;

public class PlanetBiomeProvider extends BiomeProvider {

    private final Biome biome;

    public PlanetBiomeProvider(PlanetType type) {
        this.biome = selectBiome(type);
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return biome;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return Collections.singletonList(biome);
    }


    private static Biome selectBiome(PlanetType type) {
        switch (type) {
            case TATOOINE: return Biome.DESERT;
            case DAGOBAH:  return Biome.SWAMP;
            case MUSTAFAR: return Biome.BASALT_DELTAS;
            case HOTH:
            default:       return Biome.FROZEN_OCEAN;
        }
    }
}