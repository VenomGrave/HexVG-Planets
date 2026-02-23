package com.venomgrave.hexvg.schematic;

public class Schematic {

    private final String name;
    private final int width;
    private final int height;
    private final int length;
    private final int lootMin;
    private final int lootMax;

    private final SchematicBlock[][][] blocks;

    public Schematic(String name, int width, int height, int length,
                     int lootMin, int lootMax, SchematicBlock[][][] blocks) {
        this.name    = name;
        this.width   = width;
        this.height  = height;
        this.length  = length;
        this.lootMin = lootMin;
        this.lootMax = lootMax;
        this.blocks  = blocks;
    }

    public String getName()   { return name; }
    public int getWidth()     { return width; }
    public int getHeight()    { return height; }
    public int getLength()    { return length; }
    public int getLootMin()   { return lootMin; }
    public int getLootMax()   { return lootMax; }

    public SchematicBlock getBlock(int y, int z, int x) {
        return blocks[y][z][x];
    }

    public SchematicBlock[][][] getBlocks() {
        return blocks;
    }
}
