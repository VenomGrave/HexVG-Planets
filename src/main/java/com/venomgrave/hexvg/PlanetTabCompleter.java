package com.venomgrave.hexvg;

import com.venomgrave.hexvg.world.PlanetType;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class PlanetTabCompleter implements TabCompleter {

    private static final List<String> PLANET_TYPES = Arrays.asList("hoth", "tatooine", "dagobah", "mustafar"
    );

    private static final List<String> DIRECTIONS = Arrays.asList("north", "east", "south", "west"
    );

    private static final List<String> REGION_SUBS = Arrays.asList("info", "create", "list", "flag", "remove"
    );

    private static final List<String> WORLD_FLAGS = Arrays.asList(
            "rules.dropice", "rules.droppackedice", "rules.dropsnow",
            "rules.freezewater", "rules.freezelava", "rules.stopmelt",
            "rules.lessstone", "rules.lavaburn", "rules.plantsgrow",
            "rules.grassspread", "rules.volcanoes",
            "rules.spawn.neutral.on", "rules.spawn.neutral.rarity",
            "rules.environment.suit", "rules.environment.period",
            "world.surfaceoffset", "world.generateores", "world.generatelogs"
    );

    private static final List<String> REGION_FLAGS = Arrays.asList(
            "spiderforest", "mustafarbase", "mustafartemple",
            "nospawn", "nobreak", "nobuild", "noenter",
            "lavaimmune", "freezeimmune", "heatimmune"
    );

    private final HexVGPlanets plugin;

    public PlanetTabCompleter(HexVGPlanets plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias, String[] args) {
        String name = cmd.getName().toLowerCase();

        switch (name) {

            case "planetsaddworld":
                return completeAddWorld(args);

            case "planetsdelworld":
            case "planetsworldinfo":
                return completeRegisteredWorld(args, 1);

            case "planetssetworldtype":
                return completeSetWorldType(args);

            case "planetssetworldflag":
                return completeSetWorldFlag(args);

            case "planetspaste":
                return completePaste(args);

            case "planetspos1":
            case "planetspos2":
                return Collections.emptyList();

            case "planetsregion":
                return completeRegion(args);

            case "planetsreload":
            case "planetsinfo":
            case "planetslist":
            case "planetsundo":
            case "planetssavell":
                return Collections.emptyList();

            default:
                return null;
        }
    }


    private List<String> completeAddWorld(String[] args) {
        if (args.length == 1) {
            // Podpowiedz wszystkie załadowane światy których jeszcze nie zarejestrowano
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(w -> !plugin.getWorldConfigManager().isRegistered(w))
                    .filter(w -> w.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return filterPrefix(PLANET_TYPES, args[1]);
        }
        return Collections.emptyList();
    }


    private List<String> completeRegisteredWorld(String[] args, int pos) {
        if (args.length == pos) {
            String prefix = args[pos - 1].toLowerCase();
            return plugin.getWorldConfigManager().getRegisteredWorlds().stream()
                    .filter(w -> w.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }


    private List<String> completeSetWorldType(String[] args) {
        if (args.length == 1) return completeRegisteredWorld(args, 1);
        if (args.length == 2) return filterPrefix(PLANET_TYPES, args[1]);
        return Collections.emptyList();
    }


    private List<String> completeSetWorldFlag(String[] args) {
        if (args.length == 1) return completeRegisteredWorld(args, 1);
        if (args.length == 2) return filterPrefix(WORLD_FLAGS, args[1]);
        if (args.length == 3) {
            String flag = args[1].toLowerCase();
            if (flag.startsWith("rules.") || flag.startsWith("world.generate")) {
                return filterPrefix(Arrays.asList("true", "false"), args[2]);
            }
        }
        return Collections.emptyList();
    }


    private List<String> completePaste(String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return plugin.getSchematicRegistry().getNames().stream()
                    .filter(s -> s.toLowerCase().startsWith(prefix))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return filterPrefix(DIRECTIONS, args[1]);
        }
        return Collections.emptyList();
    }


    private List<String> completeRegion(String[] args) {
        if (args.length == 1) {
            return filterPrefix(REGION_SUBS, args[0]);
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "info":
                return Collections.emptyList();

            case "flag":
                if (args.length == 3) return filterPrefix(REGION_FLAGS, args[2]);
                if (args.length == 4) return filterPrefix(Arrays.asList("true", "false"), args[3]);
                return Collections.emptyList();

            case "remove":
                return Collections.emptyList();

            default:
                return Collections.emptyList();
        }
    }


    private List<String> filterPrefix(List<String> candidates, String prefix) {
        String lp = prefix.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String c : candidates) {
            if (c.toLowerCase().startsWith(lp)) result.add(c);
        }
        return result;
    }
}