package com.venomgrave.hexvg.task;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BlockReplaceTask implements Runnable {

    private final World world;
    private final int x, y, z;
    private final Material expected;
    private final Material replacement;

    public BlockReplaceTask(World world, int x, int y, int z,
                            Material expected, Material replacement) {
        this.world       = world;
        this.x           = x;
        this.y           = y;
        this.z           = z;
        this.expected    = expected;
        this.replacement = replacement;
    }

    @Override
    public void run() {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() == expected) {
            block.setType(replacement, false);
        }
    }
}
