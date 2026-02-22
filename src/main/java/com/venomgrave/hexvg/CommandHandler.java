package com.venomgrave.hexvg;

import com.venomgrave.hexvg.loot.LootEntry;
import com.venomgrave.hexvg.loot.LootTable;
import com.venomgrave.hexvg.recipe.RecipeManager;
import com.venomgrave.hexvg.region.PlanetRegion;
import com.venomgrave.hexvg.schematic.Schematic;
import com.venomgrave.hexvg.schematic.SchematicPlacer;
import com.venomgrave.hexvg.schematic.SchematicRotator;
import com.venomgrave.hexvg.schematic.SchematicSaver;
import com.venomgrave.hexvg.util.MessageFormatter;
import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class CommandHandler {

    private CommandHandler() {}

    private static final Map<UUID, UndoSnapshot> undoHistory = new LinkedHashMap<>();

    private static class UndoSnapshot {
        final World world;
        final int x, y, z;
        final int width, height, length;
        final String[][][] blockTypes;

        UndoSnapshot(World world, int x, int y, int z, int width, int height, int length) {
            this.world  = world;
            this.x = x; this.y = y; this.z = z;
            this.width  = width;
            this.height = height;
            this.length = length;
            this.blockTypes = new String[height][length][width];
        }

        static UndoSnapshot capture(World world, int px, int py, int pz, int w, int h, int l) {
            UndoSnapshot snap = new UndoSnapshot(world, px, py, pz, w, h, l);
            for (int dy = 0; dy < h; dy++)
                for (int dz = 0; dz < l; dz++)
                    for (int dx = 0; dx < w; dx++)
                        snap.blockTypes[dy][dz][dx] =
                                world.getBlockAt(px + dx, py + dy, pz + dz)
                                        .getType().name();
            return snap;
        }

        void restore() {
            for (int dy = 0; dy < height; dy++)
                for (int dz = 0; dz < length; dz++)
                    for (int dx = 0; dx < width; dx++) {
                        try {
                            org.bukkit.Material mat = org.bukkit.Material.valueOf(blockTypes[dy][dz][dx]);
                            world.getBlockAt(x + dx, y + dy, z + dz).setType(mat, false);
                        } catch (Exception ignored) {}
                    }
        }
    }


    private static String lang(HexVGPlanets p, String key, Object... args) {
        return p.getLangManager().get(key, args);
    }


    public static boolean handle(HexVGPlanets plugin, CommandSender sender, Command cmd, String[] args) {
        switch (cmd.getName().toLowerCase()) {
            case "planetsreload":        return handleReload(plugin, sender, args);
            case "planetsinfo":          return handleInfo(plugin, sender, args);
            case "planetslist":          return handleList(plugin, sender, args);
            case "planetspaste":         return handlePaste(plugin, sender, args);
            case "planetsundo":          return handleUndo(plugin, sender);
            case "planetssave":          return handleSave(plugin, sender, args);
            case "planetsaddworld":      return handleAddWorld(plugin, sender, args);
            case "planetsdelworld":      return handleDelWorld(plugin, sender, args);
            case "planetssetworldtype":  return handleSetWorldType(plugin, sender, args);
            case "planetssetworldflag":  return handleSetWorldFlag(plugin, sender, args);
            case "planetsworldinfo":     return handleWorldInfo(plugin, sender, args);
            case "planetsregion":        return handleRegion(plugin, sender, args);
            case "planetssavell":        return handleSaveLootList(plugin, sender, args);
            case "planetspos1":          return handlePos(plugin, sender, 1);
            case "planetspos2":          return handlePos(plugin, sender, 2);
            case "planetsitem":          return handleItem(plugin, sender, args);
            default: return false;
        }
    }


    private static boolean handleReload(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("schematic")) {
            String name = args[1];
            boolean ok = plugin.getSchematicRegistry().reload(name);
            if (ok) MessageFormatter.sendOk(sender,  lang(plugin, "reload.schematic_ok",  name));
            else    MessageFormatter.sendErr(sender, lang(plugin, "reload.schematic_err", name));
            return true;
        }
        plugin.reloadConfig();
        plugin.getLangManager().reload();
        plugin.getSchematicRegistry().loadAll();
        plugin.getLootGenerator().load();
        plugin.getRegionManager().load();
        MessageFormatter.sendOk(sender, lang(plugin, "reload.done", plugin.getSchematicRegistry().size()));
        return true;
    }


    private static boolean handleInfo(HexVGPlanets plugin, CommandSender sender, String[] args) {
        sender.sendMessage(MessageFormatter.header(lang(plugin, "info.header")));
        sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.version"), plugin.getDescription().getVersion()));
        sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.schematics"), plugin.getSchematicRegistry().size()));
        sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.worlds"), plugin.getWorldConfigManager().getRegisteredWorlds().size()));

        if (sender instanceof Player) {
            Player p = (Player) sender;
            World w  = p.getWorld();
            sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.your_world"), w.getName()));
            if (plugin.isPlanetWorld(w)) {
                sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.planet_type"), plugin.getPlanetType(w).toKey(), ChatColor.GOLD));
            } else {
                sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.planet_type"), lang(plugin, "info.not_planet"), ChatColor.DARK_GRAY));
            }
            String coords = MessageFormatter.formatCoords(p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
            sender.sendMessage(MessageFormatter.kv(lang(plugin, "info.position"), coords));
        }
        return true;
    }


    private static boolean handleList(HexVGPlanets plugin, CommandSender sender, String[] args) {
        int page = 1;
        if (args.length >= 1) {
            try { page = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        List<String> lines = plugin.getSchematicRegistry().getNames().stream().sorted().map(n -> ChatColor.GRAY + "  " + ChatColor.AQUA + n).collect(Collectors.toList());
        MessageFormatter.sendPage(sender, lines, lang(plugin, "list.header"), page);
        return true;
    }


    private static boolean handlePaste(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.only_ingame"));
            return true;
        }
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "paste.usage"));
            return true;
        }

        Player player = (Player) sender;
        Optional<Schematic> opt = plugin.getSchematicRegistry().get(args[0]);
        if (!opt.isPresent()) {
            MessageFormatter.sendErr(sender, lang(plugin, "paste.not_found", args[0]));
            return true;
        }

        int dir = 2;
        if (args.length >= 2) {
            switch (args[1].toLowerCase()) {
                case "south": dir = 0; break;
                case "west":  dir = 1; break;
                case "north": dir = 2; break;
                case "east":  dir = 3; break;
            }
        }

        Schematic original = opt.get();
        Schematic rotated  = SchematicRotator.rotate(original, dir);
        Location loc = player.getLocation();
        World world  = loc.getWorld();
        int px = loc.getBlockX(), py = loc.getBlockY(), pz = loc.getBlockZ();

        UndoSnapshot snap = UndoSnapshot.capture(
                world, px, py, pz,
                rotated.getWidth(), rotated.getHeight(), rotated.getLength());
        undoHistory.put(player.getUniqueId(), snap);

        SchematicPlacer.place(world, rotated, px, py, pz, plugin.getLootGenerator());

        MessageFormatter.sendOk(sender, lang(plugin, "paste.done", args[0], dirName(plugin, dir)));
        return true;
    }


    private static boolean handleUndo(HexVGPlanets plugin, CommandSender sender) {
        if (!(sender instanceof Player)) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.only_ingame"));
            return true;
        }
        Player player = (Player) sender;
        UndoSnapshot snap = undoHistory.remove(player.getUniqueId());
        if (snap == null) {
            MessageFormatter.sendErr(sender, lang(plugin, "undo.nothing"));
            return true;
        }
        snap.restore();
        MessageFormatter.sendOk(sender, lang(plugin, "undo.done"));
        return true;
    }


    private static boolean handleSave(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.only_ingame"));
            return true;
        }
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "save.usage"));
            return true;
        }

        Player player = (Player) sender;
        String name   = args[0];

        Location pos1 = PlayerSelectionManager.getPos1(player);
        Location pos2 = PlayerSelectionManager.getPos2(player);

        if (pos1 == null || pos2 == null) {
            MessageFormatter.sendErr(sender, lang(plugin, "save.no_selection"));
            return true;
        }
        if (pos1.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            MessageFormatter.sendErr(sender, lang(plugin, "save.diff_worlds"));
            return true;
        }

        File dir = new File(plugin.getDataFolder(), "schematics");
        dir.mkdirs();
        File out = new File(dir, name + ".sm");

        try {
            SchematicSaver.save(pos1.getWorld(), pos1, pos2, out, name);
            plugin.getSchematicRegistry().reload(name);
            int w = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
            int h = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
            int l = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
            MessageFormatter.sendOk(sender, lang(plugin, "save.done", name, w, h, l));
        } catch (IOException e) {
            MessageFormatter.sendErr(sender, lang(plugin, "save.error", e.getMessage()));
            plugin.getLogger().warning("[CommandHandler] Błąd zapisu schematu " + name + ": " + e.getMessage());
        }
        return true;
    }


    private static boolean handleSaveLootList(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "savell.usage"));
            return true;
        }

        String fileName = args[0].replaceAll("[^a-zA-Z0-9_\\-]", "_");
        File lootDir    = new File(plugin.getDataFolder(), "loot");
        lootDir.mkdirs();
        File out = new File(lootDir, fileName + ".yml");

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", fileName);
        yaml.set("minItems", 3);
        yaml.set("maxItems", 10);

        String[] materials = { "IRON_INGOT","GOLD_INGOT","DIAMOND","BREAD","TORCH","OAK_PLANKS","STONE_SWORD","ARROW" };
        int[] weights = {15,5,1,10,10,8,3,8};
        int[] mins    = { 2,1,1, 1, 4,4,1,4};
        int[] maxs    = { 8,3,1, 6,16,12,1,16};

        for (int i = 0; i < materials.length; i++) {
            String path = "entries." + i;
            yaml.set(path + ".material", materials[i]);
            yaml.set(path + ".weight",   weights[i]);
            yaml.set(path + ".min",      mins[i]);
            yaml.set(path + ".max",      maxs[i]);
        }

        try {
            yaml.save(out);
            plugin.getLootGenerator().load();
            MessageFormatter.sendOk(sender, lang(plugin, "savell.done", fileName));
        } catch (IOException e) {
            MessageFormatter.sendErr(sender, lang(plugin, "savell.error", e.getMessage()));
        }
        return true;
    }


    private static boolean handleAddWorld(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "addworld.usage"));
            return true;
        }
        PlanetType type = args.length >= 2 ? PlanetType.fromString(args[1]) : PlanetType.HOTH;
        plugin.getWorldConfigManager().registerWorld(args[0], type);
        MessageFormatter.sendOk(sender, lang(plugin, "addworld.done", args[0], type.toKey()));
        return true;
    }

    private static boolean handleDelWorld(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "delworld.usage"));
            return true;
        }
        plugin.getWorldConfigManager().unregisterWorld(args[0]);
        MessageFormatter.sendOk(sender, lang(plugin, "delworld.done", args[0]));
        return true;
    }

    private static boolean handleSetWorldType(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageFormatter.sendErr(sender, lang(plugin, "setworldtype.usage"));
            return true;
        }
        boolean ok = plugin.getWorldConfigManager().setType(args[0], PlanetType.fromString(args[1]));
        if (ok) MessageFormatter.sendOk(sender,  lang(plugin, "setworldtype.done",      args[1]));
        else    MessageFormatter.sendErr(sender, lang(plugin, "setworldtype.not_found"));
        return true;
    }

    private static boolean handleSetWorldFlag(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageFormatter.sendErr(sender, lang(plugin, "setworldflag.usage"));
            return true;
        }
        String val = args.length >= 3 ? args[2] : "";
        boolean ok = plugin.getWorldConfigManager().setFlag(args[0], args[1], val);
        if (ok) MessageFormatter.sendOk(sender,  lang(plugin, "setworldflag.done",      args[1], val));
        else    MessageFormatter.sendErr(sender, lang(plugin, "setworldflag.not_found"));
        return true;
    }

    private static boolean handleWorldInfo(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (args.length >= 1) {
            String info = plugin.getWorldConfigManager().getWorldInfo(args[0]);
            if (info != null) {
                sender.sendMessage(MessageFormatter.header(lang(plugin, "worldinfo.header", args[0])));
                sender.sendMessage(info);
            } else {
                MessageFormatter.sendErr(sender, lang(plugin, "worldinfo.not_found"));
            }
        } else {
            Set<String> worlds = plugin.getWorldConfigManager().getRegisteredWorlds();
            sender.sendMessage(MessageFormatter.header(
                    lang(plugin, "worldinfo.header_all", worlds.size())));
            worlds.forEach(w -> {
                String info = plugin.getWorldConfigManager().getWorldInfo(w);
                sender.sendMessage(MessageFormatter.kv(w, info != null ? info : ""));
            });
        }
        return true;
    }


    private static boolean handleRegion(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.only_ingame"));
            return true;
        }
        if (args.length == 0) {
            MessageFormatter.sendErr(sender, lang(plugin, "region.usage"));
            return true;
        }

        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "info": {
                List<PlanetRegion> regions =
                        plugin.getRegionManager().getRegionsAt(player.getLocation());
                if (regions.isEmpty()) {
                    MessageFormatter.sendInfo(sender, lang(plugin, "region.not_in_region"));
                } else {
                    sender.sendMessage(MessageFormatter.header(lang(plugin, "region.header_at")));
                    regions.forEach(r -> {
                        sender.sendMessage(MessageFormatter.kv(r.getName(), r.toString()));
                        r.getFlags().forEach((k, v) ->
                                sender.sendMessage(MessageFormatter.kv("  " + k, v, ChatColor.YELLOW)));
                    });
                }
                return true;
            }
            case "flag": {
                if (args.length < 3) {
                    MessageFormatter.sendErr(sender, lang(plugin, "region.usage_flag"));
                    return true;
                }
                plugin.getRegionManager().getRegion(args[1]).ifPresentOrElse(r -> {
                    r.setFlag(args[2], args.length >= 4 ? args[3] : "true");
                    plugin.getRegionManager().save();
                    MessageFormatter.sendOk(sender, lang(plugin, "region.flag_set", args[2]));
                }, () -> MessageFormatter.sendErr(sender, lang(plugin, "region.not_found", args[1])));
                return true;
            }
            case "remove": {
                if (args.length < 2) {
                    MessageFormatter.sendErr(sender, lang(plugin, "region.usage_remove"));
                    return true;
                }
                boolean removed = plugin.getRegionManager().removeRegion(args[1]);
                if (removed) MessageFormatter.sendOk(sender,  lang(plugin, "region.removed",   args[1]));
                else         MessageFormatter.sendErr(sender, lang(plugin, "region.not_found", args[1]));
                return true;
            }
            case "create": {
                return handleRegionCreate(plugin, sender, player, args);
            }
            case "list": {
                return handleRegionList(plugin, sender, args);
            }
            default:
                MessageFormatter.sendErr(sender, lang(plugin, "region.usage"));
                return true;
        }
    }

    private static boolean handleRegionCreate(HexVGPlanets plugin, CommandSender sender,
                                              Player player, String[] args) {
        if (args.length < 2) {
            MessageFormatter.sendErr(sender, lang(plugin, "region.usage_create"));
            return true;
        }
        Location p1 = PlayerSelectionManager.getPos1(player);
        Location p2 = PlayerSelectionManager.getPos2(player);
        if (p1 == null || p2 == null) {
            MessageFormatter.sendErr(sender, lang(plugin, "region.no_selection"));
            return true;
        }
        if (p1.getWorld() == null || !p1.getWorld().equals(p2.getWorld())) {
            MessageFormatter.sendErr(sender, lang(plugin, "region.diff_worlds"));
            return true;
        }

        String name = args[1];
        PlanetRegion region = new PlanetRegion(
                name, p1.getWorld().getName(),
                p1.getBlockX(), p1.getBlockY(), p1.getBlockZ(),
                p2.getBlockX(), p2.getBlockY(), p2.getBlockZ()
        );

        for (int i = 2; i < args.length; i++) {
            String flag = args[i];
            if (flag.contains("=")) {
                String[] kv = flag.split("=", 2);
                region.setFlag(kv[0], kv[1]);
            } else {
                region.setFlag(flag, "true");
            }
        }

        plugin.getRegionManager().addRegion(region);
        PlayerSelectionManager.clearSelection(player);

        int w = Math.abs(p2.getBlockX() - p1.getBlockX()) + 1;
        int h = Math.abs(p2.getBlockY() - p1.getBlockY()) + 1;
        int l = Math.abs(p2.getBlockZ() - p1.getBlockZ()) + 1;
        MessageFormatter.sendOk(sender, lang(plugin, "region.created", name, w, h, l));
        if (!region.getFlags().isEmpty()) {
            sender.sendMessage(lang(plugin, "region.created_flags", region.getFlags().toString()));
        }
        return true;
    }

    private static boolean handleRegionList(HexVGPlanets plugin, CommandSender sender, String[] args) {
        int page = args.length >= 2 ? parseIntSafe(args[1], 1) : 1;

        List<String> lines = new ArrayList<>();
        plugin.getRegionManager().getAllRegions().forEach(r -> {
            String flagStr = r.getFlags().isEmpty() ? ""
                    : " &8(" + String.join(", ", r.getFlags().keySet()) + ")";
            lines.add(MessageFormatter.kv(r.getName(),
                    r.getWorld() + " " +
                            MessageFormatter.formatCoords(r.getMinX(), r.getMinY(), r.getMinZ()) + " → " +
                            MessageFormatter.formatCoords(r.getMaxX(), r.getMaxY(), r.getMaxZ()))
                    + ChatColor.DARK_GRAY + flagStr);
        });

        if (lines.isEmpty()) {
            MessageFormatter.sendInfo(sender, lang(plugin, "region.list_empty"));
        } else {
            MessageFormatter.sendPage(sender, lines, lang(plugin, "region.list_header"), page);
        }
        return true;
    }

    private static boolean handlePos(HexVGPlanets plugin, CommandSender sender, int num) {
        if (!(sender instanceof Player)) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.only_ingame"));
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        if (num == 1) PlayerSelectionManager.setPos1(player, loc);
        else          PlayerSelectionManager.setPos2(player, loc);

        String coords = MessageFormatter.formatCoords(loc.getX(), loc.getY(), loc.getZ());
        MessageFormatter.sendInfo(sender, lang(plugin, "pos.set", num, coords));

        Location p1 = PlayerSelectionManager.getPos1(player);
        Location p2 = PlayerSelectionManager.getPos2(player);
        if (p1 != null && p2 != null && p1.getWorld() != null && p1.getWorld().equals(p2.getWorld())) {
            int w = Math.abs(p2.getBlockX() - p1.getBlockX()) + 1;
            int h = Math.abs(p2.getBlockY() - p1.getBlockY()) + 1;
            int l = Math.abs(p2.getBlockZ() - p1.getBlockZ()) + 1;
            MessageFormatter.sendInfo(sender, lang(plugin, "pos.selection", w, h, l, (w * h * l)));
        }
        return true;
    }


    private static boolean handleItem(HexVGPlanets plugin, CommandSender sender, String[] args) {
        if (!sender.hasPermission("hexvg.planets.item")) {
            MessageFormatter.sendErr(sender, lang(plugin, "general.no_permission"));
            return true;
        }
        if (args.length < 1) {
            MessageFormatter.sendErr(sender, lang(plugin, "item.usage"));
            MessageFormatter.sendInfo(sender, lang(plugin, "item.usage_hint"));
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                MessageFormatter.sendErr(sender, lang(plugin, "item.not_online", args[1]));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageFormatter.sendErr(sender, lang(plugin, "item.player_only"));
                return true;
            }
            target = (Player) sender;
        }

        String tag, label;

        switch (args[0].toLowerCase()) {
            case "warm": case "hoth":
                tag = RecipeManager.WARM_SUIT_TAG;      label = "Warm Suit";      break;
            case "cooling": case "cool": case "tatooine":
                tag = RecipeManager.COOLING_SUIT_TAG;   label = "Cooling Suit";   break;
            case "repellent": case "rep": case "dagobah":
                tag = RecipeManager.REPELLENT_SUIT_TAG; label = "Repellent Suit"; break;
            case "glass": case "mustafar":
                tag = RecipeManager.GLASS_SUIT_TAG;     label = "Glass Suit";     break;
            default:
                MessageFormatter.sendErr(sender, lang(plugin, "item.unknown", args[0]));
                return true;
        }

        Material mat = Material.LEATHER_HELMET;
        target.getInventory().addItem(
                RecipeManager.makeSuitItem(mat,                    label + " Helmet"),
                RecipeManager.makeSuitItem(Material.LEATHER_CHESTPLATE, label + " Chestplate"),
                RecipeManager.makeSuitItem(Material.LEATHER_LEGGINGS,   label + " Leggings"),
                RecipeManager.makeSuitItem(Material.LEATHER_BOOTS,      label + " Boots")
        );

        MessageFormatter.sendInfo(sender, lang(plugin, "item.given", label, target.getName()));
        if (!target.equals(sender)) {
            target.sendMessage(lang(plugin, "item.received", label));
        }
        return true;
    }

    private static String dirName(HexVGPlanets plugin, int dir) {
        switch (dir) {
            case 0: return lang(plugin, "paste.dir.south");
            case 1: return lang(plugin, "paste.dir.west");
            case 3: return lang(plugin, "paste.dir.east");
            default: return lang(plugin, "paste.dir.north");
        }
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}