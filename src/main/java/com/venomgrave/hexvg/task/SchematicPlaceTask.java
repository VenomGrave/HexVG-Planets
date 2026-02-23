package com.venomgrave.hexvg.task;

import com.venomgrave.hexvg.HexVGPlanets;
import com.venomgrave.hexvg.loot.LootGenerator;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;
import org.bukkit.World;

public class SchematicPlaceTask implements Runnable {

    private final HexVGPlanets plugin;
    private final World world;
    private final Schematic schematic;
    private final int x, y, z;
    private final int direction;
    private final LootGenerator lootGenerator;

    public SchematicPlaceTask(HexVGPlanets plugin, World world,
                              Schematic schematic, int x, int y, int z, int direction,
                              LootGenerator lootGenerator) {
        this.plugin        = plugin;
        this.world         = world;
        this.schematic     = schematic;
        this.x             = x;
        this.y             = y;
        this.z             = z;
        this.direction     = direction;
        this.lootGenerator = lootGenerator;
    }

    @Override
    public void run() {
        Schematic rotated = SchematicRotator.rotate(schematic, direction);
        SchematicPlacer.place(world, rotated, x, y, z, lootGenerator);
    }
}
