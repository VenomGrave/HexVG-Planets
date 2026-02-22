package com.venomgrave.hexvg.world;

/**
 * Represents the planet type of a registered world.
 * Replaces the old WorldType enum.
 */
public enum PlanetType {

    HOTH,
    TATOOINE,
    DAGOBAH,
    MUSTAFAR;

    /**
     * Parse a string (from config or /planetsaddworld) to a PlanetType.
     * Falls back to HOTH on unknown values.
     */
    public static PlanetType fromString(String s) {
        if (s == null) return HOTH;
        switch (s.toLowerCase().trim()) {
            case "tatooine": return TATOOINE;
            case "dagobah":  return DAGOBAH;
            case "mustafar": return MUSTAFAR;
            default:         return HOTH;
        }
    }

    /** Lowercase config-friendly name, e.g. "hoth" */
    public String toKey() {
        return name().toLowerCase();
    }
}