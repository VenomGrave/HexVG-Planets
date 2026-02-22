package com.venomgrave.hexvg.schematic;

import org.bukkit.Material;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class SchematicLoader {

    public static Schematic load(InputStream in, String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String schName = name;
        int width = 0, height = 0, length = 0;
        int lootMin = 2, lootMax = 10;
        List<int[]> coords    = new ArrayList<>();
        List<SchematicBlock> blockList = new ArrayList<>();
        boolean inBlocks = false;

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("name:")) {
                schName = line.substring(5).trim();
                continue;
            }
            if (line.startsWith("size:")) {
                String[] parts = line.substring(5).trim().split("\\s+");
                width  = Integer.parseInt(parts[0]);
                height = Integer.parseInt(parts[1]);
                length = Integer.parseInt(parts[2]);
                continue;
            }
            if (line.startsWith("loot:")) {
                String[] parts = line.substring(5).trim().split("\\s+");
                lootMin = Integer.parseInt(parts[0]);
                lootMax = Integer.parseInt(parts[1]);
                continue;
            }
            if (line.equals("blocks:")) {
                inBlocks = true;
                continue;
            }
            if (!inBlocks) continue;

            String[] parts = line.split("\\s+", 5);
            if (parts.length < 4) continue;

            int by = Integer.parseInt(parts[0]);
            int bz = Integer.parseInt(parts[1]);
            int bx = Integer.parseInt(parts[2]);
            String matName = parts[3].toUpperCase();

            SchematicBlock block;
            if (matName.equals("SPAWNER") && parts.length >= 5) {
                block = SchematicBlock.spawner(parts[4].toUpperCase());
            } else if (matName.equals("ENTITY") && parts.length >= 5) {
                block = SchematicBlock.entitySpawn(parts[4].toUpperCase());
            } else {
                Material mat = Material.matchMaterial(matName);
                if (mat == null) mat = Material.AIR;
                String bdString = parts.length >= 5 ? parts[4] : null;
                block = new SchematicBlock(mat, bdString);
            }

            coords.add(new int[]{by, bz, bx});
            blockList.add(block);
        }
        reader.close();

        SchematicBlock[][][] blocks = new SchematicBlock[height][length][width];
        for (int y = 0; y < height; y++)
            for (int z = 0; z < length; z++)
                for (int x = 0; x < width; x++)
                    blocks[y][z][x] = SchematicBlock.AIR;

        for (int i = 0; i < coords.size(); i++) {
            int[] c = coords.get(i);
            if (c[0] < height && c[1] < length && c[2] < width) {
                blocks[c[0]][c[1]][c[2]] = blockList.get(i);
            }
        }

        return new Schematic(schName, width, height, length, lootMin, lootMax, blocks);
    }
}