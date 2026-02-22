package com.venomgrave.hexvg;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerSelectionManager {

    private PlayerSelectionManager() {}

    private static final Map<UUID, Location> pos1Map = new HashMap<>();
    private static final Map<UUID, Location> pos2Map = new HashMap<>();

    public static void setPos1(Player p, Location loc) { pos1Map.put(p.getUniqueId(), loc.clone()); }
    public static void setPos2(Player p, Location loc) { pos2Map.put(p.getUniqueId(), loc.clone()); }

    public static Location getPos1(Player p) { return pos1Map.get(p.getUniqueId()); }
    public static Location getPos2(Player p) { return pos2Map.get(p.getUniqueId()); }

    public static void clearSelection(Player p) {
        UUID id = p.getUniqueId();
        pos1Map.remove(id);
        pos2Map.remove(id);
    }
}