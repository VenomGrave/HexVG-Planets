package com.venomgrave.hexvg.schematic;

import com.venomgrave.hexvg.HexVGPlanets;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;


public class SchematicRegistry {

    private final HexVGPlanets plugin;
    private final Map<String, Schematic> schematics = new LinkedHashMap<>();

    public SchematicRegistry(HexVGPlanets plugin) {
        this.plugin = plugin;
    }


    public void loadAll() {
        schematics.clear();
        loadFromDataFolder();
        loadBuiltIn();
        plugin.getLogger().info("Załadowano " + schematics.size() + " schematów.");
    }


    private void loadFromDataFolder() {
        File dir = new File(plugin.getDataFolder(), "schematics");
        if (!dir.exists()) { dir.mkdirs(); return; }

        File[] files = dir.listFiles((d, n) -> n.endsWith(".sm"));
        if (files == null) return;

        for (File f : files) {
            if (f.getName().contains("_converted")) continue;
            loadFile(f, true);
        }
    }

    private void loadFile(File file, boolean autoConvert) {
        String name = file.getName().replace(".sm", "");
        try {
            boolean legacy = false;
            boolean matrix = false;
            try (InputStream check = new FileInputStream(file)) {
                matrix = MatrixSchematicConverter.isMatrixFormat(check);
            }
            if (!matrix) {
                try (InputStream check = new FileInputStream(file)) {
                    legacy = SchematicConverter.isLegacyFormat(check);
                }
            }

            if (matrix && autoConvert) {
                plugin.getLogger().info("[SchematicRegistry] Konwertuję format macierzowy: " + file.getName());
                File converted = new File(file.getParentFile(), name + "_converted.sm");
                MatrixSchematicConverter.convertFile(file, converted, name, plugin.getLogger());
                try (InputStream is = new FileInputStream(converted)) {
                    Schematic s = SchematicLoader.load(is, name);
                    schematics.put(s.getName().toLowerCase(), s);
                    plugin.getLogger().info("[SchematicRegistry] Załadowano (matrix→nowy): " + s.getName());
                }
            } else if (legacy && autoConvert) {
                plugin.getLogger().info("[SchematicRegistry] Konwertuję legacy schemat: " + file.getName());
                File converted = new File(file.getParentFile(), name + "_converted.sm");
                SchematicConverter.convertFile(file, converted, name, plugin.getLogger());
                try (InputStream is = new FileInputStream(converted)) {
                    Schematic s = SchematicLoader.load(is, name);
                    schematics.put(s.getName().toLowerCase(), s);
                    plugin.getLogger().info("[SchematicRegistry] Załadowano (legacy→nowy): " + s.getName());
                }
            } else {
                try (InputStream is = new FileInputStream(file)) {
                    Schematic s = SchematicLoader.load(is, name);
                    schematics.put(s.getName().toLowerCase(), s);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[SchematicRegistry] Błąd ładowania: " + file.getName(), e);
        }
    }


    private static final String[] BUILT_INS = {
            "schematics/hoth/base_room1.sm",
            "schematics/hoth/base_room2.sm",
            "schematics/hoth/dome.sm",
            "schematics/hoth/minidome.sm",
            "schematics/hoth/garden.sm",
            "schematics/tatooine/oasis.sm",
            "schematics/tatooine/sandcastle.sm",
            "schematics/tatooine/sarlacc.sm",
            "schematics/tatooine/skeleton.sm",
            "schematics/tatooine/villagecenter.sm",
            "schematics/tatooine/villagehut1.sm",
            "schematics/tatooine/villagehut2.sm",
            "schematics/tatooine/villagehut3.sm",
            "schematics/tatooine/villagehut4.sm",
            "schematics/tatooine/villagehut5.sm",
            "schematics/tatooine/villagehut6.sm",
            "schematics/tatooine/villagehut7.sm",
            "schematics/tatooine/villagehut8.sm",
            "schematics/dagobah/mushroomhut1.sm",
            "schematics/dagobah/mushroomhut2.sm",
            "schematics/dagobah/mushroomhut3.sm",
            "schematics/dagobah/mushroomhut4.sm",
            "schematics/dagobah/mushroomhut5.sm",
            "schematics/dagobah/mushroomhut6.sm",
            "schematics/dagobah/swamptemple.sm",
            "schematics/dagobah/treehut.sm",
            "schematics/dagobah/supergarden.sm",
            "schematics/mustafar/mustafartemple.sm",
            "schematics/mustafar/mustafar_main1.sm",
            "schematics/mustafar/mustafar_main2.sm",
            "schematics/mustafar/mustafar_main3.sm",
            "schematics/mustafar/mustafar_main4.sm",
            "schematics/mustafar/mustafar_main5.sm",
            "schematics/mustafar/mustafar_e1.sm",
            "schematics/mustafar/mustafar_e2.sm",
            "schematics/mustafar/mustafar_esw1.sm",
            "schematics/mustafar/mustafar_ew1.sm",
            "schematics/mustafar/mustafar_ew2.sm",
            "schematics/mustafar/mustafar_ew3.sm",
            "schematics/mustafar/mustafar_ew4.sm",
            "schematics/mustafar/mustafar_landing.sm",
            "schematics/mustafar/mustafar_nesw1.sm",
            "schematics/mustafar/mustafar_nesw2.sm",
            "schematics/mustafar/mustafar_sw1.sm",
    };

    private void loadBuiltIn() {
        for (String path : BUILT_INS) {
            String name = path.substring(path.lastIndexOf('/') + 1).replace(".sm", "");
            try {
                byte[] data;
                try (InputStream raw = plugin.getResource(path)) {
                    if (raw == null) continue;
                    data = raw.readAllBytes();
                }

                boolean matrix = MatrixSchematicConverter.isMatrixFormat(
                        new java.io.ByteArrayInputStream(data));
                boolean legacy = !matrix && SchematicConverter.isLegacyFormat(
                        new java.io.ByteArrayInputStream(data));

                Schematic s;
                if (matrix) {
                    String converted = MatrixSchematicConverter.convertStream(
                            new java.io.ByteArrayInputStream(data), name, plugin.getLogger());
                    s = SchematicLoader.load(
                            new java.io.ByteArrayInputStream(
                                    converted.getBytes(java.nio.charset.StandardCharsets.UTF_8)), name);
                } else if (legacy) {
                    String converted = SchematicConverter.convertStream(
                            new java.io.ByteArrayInputStream(data), name, plugin.getLogger());
                    s = SchematicLoader.load(
                            new java.io.ByteArrayInputStream(
                                    converted.getBytes(java.nio.charset.StandardCharsets.UTF_8)), name);
                } else {
                    s = SchematicLoader.load(
                            new java.io.ByteArrayInputStream(data), name);
                }

                schematics.putIfAbsent(s.getName().toLowerCase(), s);

            } catch (Exception e) {
                plugin.getLogger().log(Level.FINE,
                        "[SchematicRegistry] Brak wbudowanego schematu: " + path);
            }
        }
    }

    public boolean reload(String name) {
        File dir = new File(plugin.getDataFolder(), "schematics");
        for (String suffix : new String[]{"_converted.sm", ".sm"}) {
            File f = new File(dir, name + suffix);
            if (f.exists()) {
                try (InputStream is = new FileInputStream(f)) {
                    Schematic s = SchematicLoader.load(is, name);
                    schematics.put(s.getName().toLowerCase(), s);
                    plugin.getLogger().info("[SchematicRegistry] Przeładowano: " + name);
                    return true;
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "[SchematicRegistry] Błąd reloadu: " + name, e);
                    return false;
                }
            }
        }
        plugin.getLogger().warning("[SchematicRegistry] Nie znaleziono pliku dla: " + name);
        return false;
    }

    public void register(Schematic schematic) {
        schematics.put(schematic.getName().toLowerCase(), schematic);
    }

    public void register(Schematic schematic, boolean saveToFile) {
        register(schematic);
        if (!saveToFile) return;

        File dir = new File(plugin.getDataFolder(), "schematics");
        dir.mkdirs();
        File out = new File(dir, schematic.getName() + ".sm");

        try (Writer w = new java.io.OutputStreamWriter(new FileOutputStream(out), java.nio.charset.StandardCharsets.UTF_8)) {
            w.write("# Saved by HexVG-Planets\n");
            w.write("name: " + schematic.getName() + "\n");
            w.write("size: " + schematic.getWidth() + " " + schematic.getHeight() + " " + schematic.getLength() + "\n");
            w.write("loot: " + schematic.getLootMin() + " " + schematic.getLootMax() + "\n");
            w.write("blocks:\n");
            for (int y = 0; y < schematic.getHeight(); y++) {
                for (int z = 0; z < schematic.getLength(); z++) {
                    for (int x = 0; x < schematic.getWidth(); x++) {
                        SchematicBlock b = schematic.getBlock(y, z, x);
                        if (b == null || b.isSkip()) continue;
                        if (b.getMaterial() == null) continue;
                        if (b.getMaterial().isAir()) continue;
                        String line = y + " " + z + " " + x + " " + b.getMaterial().name();
                        if (b.getBlockDataString() != null) line += " " + b.getBlockDataString();
                        w.write(line + "\n");
                    }
                }
            }
            plugin.getLogger().info("[SchematicRegistry] Zapisano: " + out.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[SchematicRegistry] Błąd zapisu: " + schematic.getName(), e);
        }
    }


    public Optional<Schematic> get(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(schematics.get(name.toLowerCase()));
    }

    public Collection<Schematic> getAll() {
        return Collections.unmodifiableCollection(schematics.values());
    }

    public Set<String> getNames() {
        return Collections.unmodifiableSet(schematics.keySet());
    }

    public int size() {
        return schematics.size();
    }

    public boolean contains(String name) {
        return name != null && schematics.containsKey(name.toLowerCase());
    }
}