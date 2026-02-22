package com.venomgrave.hexvg.schematic;

import org.bukkit.Material;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public final class MatrixSchematicConverter {

    private MatrixSchematicConverter() {}

    public static boolean isMatrixFormat(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));
        String first = reader.readLine();
        if (first == null) return false;
        first = first.trim().toUpperCase();
        return first.startsWith("ENABLED:") || first.startsWith("WIDTH:") || first.startsWith("HEIGHT:") || first.startsWith("MATRIX:");
    }

    public static void convertFile(File input, File output, String name, Logger log)
            throws IOException {
        String result;
        try (InputStream is = new FileInputStream(input)) {
            result = convertStream(is, name, log);
        }
        try (Writer w = new OutputStreamWriter(new FileOutputStream(output),
                StandardCharsets.UTF_8)) {
            w.write(result);
        }
    }

    public static String convertStream(InputStream in, String name, Logger log)
            throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8));

        int width    = 0;
        int length   = 0;
        int height   = 0;
        int lootMin  = 1;
        int lootMax  = 5;
        boolean inMatrix = false;

        int[][][] layers = null;
        int currentLayer = -1;
        int currentZ     = 0;

        String line;
        int unknownCount = 0;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                if (inMatrix && layers != null) {
                    String low = trimmed.toLowerCase();
                    if (low.contains("layer")) {
                        try {
                            String numStr = low.replace("#", "").replace("layer", "").trim();
                            currentLayer = Integer.parseInt(numStr);
                            currentZ = 0;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                continue;
            }

            if (trimmed.isEmpty()) continue;

            if (!inMatrix) {
                String upper = trimmed.toUpperCase();
                if (upper.startsWith("WIDTH:")) {
                    width  = parseHeaderInt(trimmed);
                } else if (upper.startsWith("LENGTH:")) {
                    length = parseHeaderInt(trimmed);
                } else if (upper.startsWith("HEIGHT:")) {
                    height = parseHeaderInt(trimmed);
                } else if (upper.startsWith("LOOTMIN:")) {
                    lootMin = parseHeaderInt(trimmed);
                } else if (upper.startsWith("LOOTMAX:")) {
                    lootMax = parseHeaderInt(trimmed);
                } else if (upper.startsWith("MATRIX:")) {
                    inMatrix = true;
                    if (width > 0 && height > 0 && length > 0) {
                        layers = new int[height][length][width];
                        for (int y = 0; y < height; y++)
                            for (int z = 0; z < length; z++)
                                for (int x = 0; x < width; x++)
                                    layers[y][z][x] = -1;
                    }
                }
                continue;
            }

            if (layers == null) continue;
            if (currentLayer < 0 || currentLayer >= height) continue;
            if (currentZ >= length) continue;

            String[] tokens = trimmed.split("[,\\s]+");

            if (tokens.length < width) continue;

            for (int x = 0; x < width && x < tokens.length; x++) {
                try {
                    layers[currentLayer][currentZ][x] = Integer.parseInt(tokens[x].trim());
                } catch (NumberFormatException ignored) {
                    layers[currentLayer][currentZ][x] = 0;
                }
            }
            currentZ++;
        }
        reader.close();

        if (layers == null) {
            throw new IOException("Nieprawidłowy format macierzowy: brak sekcji MATRIX " + "lub wymiary nie zostały zdefiniowane (width=" + width + " height=" + height + " length=" + length + ")");
        }

        List<String> blockLines = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int blockId = layers[y][z][x];

                    if (blockId == -1) continue;

                    if (blockId == 0) continue;

                    if (blockId == -2) {
                        blockLines.add(y + " " + z + " " + x + " SPAWNER ZOMBIE");
                        continue;
                    }

                    if (blockId < 0) continue;

                    Material mat = SchematicConverter.getLegacyMaterial(blockId);
                    if (mat == null) {
                        if (log != null) {
                            log.warning("[MatrixSchematicConverter] Nieznany block ID " + blockId + " w '" + name + "' y=" + y + " z=" + z + " x=" + x + ", zastępuję STONE");
                        }
                        mat = Material.STONE;
                        unknownCount++;
                    }

                    if (mat == Material.AIR) continue;

                    blockLines.add(y + " " + z + " " + x + " " + mat.name());
                }
            }
        }

        if (log != null && unknownCount > 0) {
            log.warning("[MatrixSchematicConverter] Schemat '" + name + "': " + unknownCount + " nieznanych bloków zastąpionych STONE.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# Converted from HothGeneratorReload matrix format\n");
        sb.append("name: ").append(name).append("\n");
        sb.append("size: ").append(width).append(" ").append(height).append(" ").append(length).append("\n");
        sb.append("loot: ").append(lootMin).append(" ").append(lootMax).append("\n");
        sb.append("blocks:\n");
        for (String bl : blockLines) {
            sb.append(bl).append("\n");
        }
        return sb.toString();
    }


    private static int parseHeaderInt(String line) {
        int colon = line.indexOf(':');
        if (colon < 0) return 0;
        try {
            return Integer.parseInt(line.substring(colon + 1).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}