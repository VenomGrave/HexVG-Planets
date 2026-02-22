package com.venomgrave.hexvg.schematic;

import org.bukkit.Material;


public class SchematicBlock {

    public static final SchematicBlock AIR = new SchematicBlock(Material.AIR, null);
    public static final SchematicBlock SKIP = new SchematicBlock(null, null);

    private final Material material;

    private final String blockDataString;

    private String spawnerEntityType;

    private String entityTypeName;
    private boolean isEntitySpawn;


    public SchematicBlock(Material material, String blockDataString) {
        this.material        = material;
        this.blockDataString = blockDataString;
        this.isEntitySpawn   = false;
    }

    public static SchematicBlock spawner(String entityType) {
        SchematicBlock b = new SchematicBlock(Material.SPAWNER, null);
        b.spawnerEntityType = entityType;
        return b;
    }

    public static SchematicBlock entitySpawn(String entityType) {
        SchematicBlock b = new SchematicBlock(null, null);
        b.isEntitySpawn  = true;
        b.entityTypeName = entityType;
        return b;
    }


    public Material getMaterial()          { return material; }
    public String getBlockDataString()     { return blockDataString; }
    public String getSpawnerEntityType()   { return spawnerEntityType; }
    public String getEntityTypeName()      { return entityTypeName; }
    public boolean isEntitySpawn()         { return isEntitySpawn; }
    public boolean isSkip()                { return material == null && !isEntitySpawn; }
}