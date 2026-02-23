package com.venomgrave.hexvg.schematic;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.Stairs;

public class SchematicRotator {

    public static Schematic rotate(Schematic original, int direction) {
        // 0 = SOUTH (180°), 1 = WEST (270°), 2 = NORTH (0°), 3 = EAST (90°)
        if (direction == 2) {
            // NORTH = 0°, brak rotacji
            return original;
        }

        int srcWidth  = original.getWidth();
        int srcLength = original.getLength();
        int srcHeight = original.getHeight();

        int dstWidth, dstLength;
        if (direction == 1 || direction == 3) {
            dstWidth  = srcLength;
            dstLength = srcWidth;
        } else {
            dstWidth  = srcWidth;
            dstLength = srcLength;
        }

        SchematicBlock[][][] dst = new SchematicBlock[srcHeight][dstLength][dstWidth];

        for (int y = 0; y < srcHeight; y++) {
            for (int z = 0; z < srcLength; z++) {
                for (int x = 0; x < srcWidth; x++) {

                    SchematicBlock sb = original.getBlock(y, z, x);
                    if (sb == null) continue;

                    int nx, nz;
                    switch (direction) {
                        case 0: // SOUTH (180°)
                            nx = srcWidth  - 1 - x;
                            nz = srcLength - 1 - z;
                            break;
                        case 1: // WEST (270°)
                            nx = z;
                            nz = srcWidth - 1 - x;
                            break;
                        case 3: // EAST (90°)
                            nx = srcLength - 1 - z;
                            nz = x;
                            break;
                        default: // NORTH (0°)
                            nx = x;
                            nz = z;
                            break;
                    }

                    dst[y][nz][nx] = rotateBlock(sb, direction);
                }
            }
        }

        return new Schematic(
                original.getName(),
                dstWidth, srcHeight, dstLength,
                original.getLootMin(), original.getLootMax(),
                dst
        );
    }

    private static SchematicBlock rotateBlock(SchematicBlock sb, int dir) {
        if (sb == null || sb.isSkip() || sb.getMaterial() == null) return sb;
        if (sb.getBlockDataString() == null || sb.getBlockDataString().isEmpty()) return sb;

        try {
            BlockData bd = Bukkit.createBlockData(sb.getBlockDataString());
            rotateBlockData(bd, dir);
            return new SchematicBlock(sb.getMaterial(), bd.getAsString());
        } catch (Exception e) {
            return sb;
        }
    }

    private static void rotateBlockData(BlockData bd, int dir) {
        int turns = turnsFor(dir);
        if (turns == 0) return;

        // Directional (furnaces, chests, stairs, etc.)
        if (bd instanceof Directional d) {
            BlockFace f = d.getFacing();
            for (int i = 0; i < turns; i++) {
                f = rotateFaceClockwise(f);
            }
            d.setFacing(f);
        }

        // Rotatable (skulls, item frames)
        if (bd instanceof Rotatable r) {
            BlockFace rot = r.getRotation();
            if (rot != null) {
                for (int i = 0; i < turns; i++) {
                    rot = rotateFaceClockwise(rot);
                }
                r.setRotation(rot);
            }
        }

        // Stairs
        if (bd instanceof Stairs s) {
            BlockFace f = s.getFacing();
            for (int i = 0; i < turns; i++) {
                f = rotateFaceClockwise(f);
            }
            s.setFacing(f);
        }

        // Orientable (logs, pillars) – Axis z org.bukkit.Axis
        if (bd instanceof Orientable o) {
            Axis axis = o.getAxis();
            if (turns % 2 == 1) {
                if (axis == Axis.X) {
                    o.setAxis(Axis.Z);
                } else if (axis == Axis.Z) {
                    o.setAxis(Axis.X);
                }
            }
        }

        // MultipleFacing (fences, walls, iron bars)
        if (bd instanceof MultipleFacing mf) {
            rotateMultipleFacing(mf, turns);
        }

        // FenceGate
        if (bd instanceof Gate gate) {
            BlockFace f = gate.getFacing();
            for (int i = 0; i < turns; i++) {
                f = rotateFaceClockwise(f);
            }
            gate.setFacing(f);
        }

        // TrapDoor
        if (bd instanceof TrapDoor td) {
            BlockFace f = td.getFacing();
            for (int i = 0; i < turns; i++) {
                f = rotateFaceClockwise(f);
            }
            td.setFacing(f);
        }

        // Door
        if (bd instanceof Door door) {
            BlockFace f = door.getFacing();
            for (int i = 0; i < turns; i++) {
                f = rotateFaceClockwise(f);
            }
            door.setFacing(f);
        }

        // RedstoneWire
        if (bd instanceof RedstoneWire wire) {
            rotateRedstoneWire(wire, turns);
        }
    }

    private static void rotateMultipleFacing(MultipleFacing mf, int turns) {
        boolean n = mf.hasFace(BlockFace.NORTH);
        boolean e = mf.hasFace(BlockFace.EAST);
        boolean s = mf.hasFace(BlockFace.SOUTH);
        boolean w = mf.hasFace(BlockFace.WEST);

        for (int i = 0; i < turns; i++) {
            boolean oldN = n, oldE = e, oldS = s, oldW = w;
            n = oldW;
            e = oldN;
            s = oldE;
            w = oldS;
        }

        mf.setFace(BlockFace.NORTH, n);
        mf.setFace(BlockFace.EAST,  e);
        mf.setFace(BlockFace.SOUTH, s);
        mf.setFace(BlockFace.WEST,  w);
    }

    private static void rotateRedstoneWire(RedstoneWire wire, int turns) {
        RedstoneWire.Connection n = wire.getFace(BlockFace.NORTH);
        RedstoneWire.Connection e = wire.getFace(BlockFace.EAST);
        RedstoneWire.Connection s = wire.getFace(BlockFace.SOUTH);
        RedstoneWire.Connection w = wire.getFace(BlockFace.WEST);

        for (int i = 0; i < turns; i++) {
            RedstoneWire.Connection oldN = n, oldE = e, oldS = s, oldW = w;
            n = oldW;
            e = oldN;
            s = oldE;
            w = oldS;
        }

        wire.setFace(BlockFace.NORTH, n);
        wire.setFace(BlockFace.EAST,  e);
        wire.setFace(BlockFace.SOUTH, s);
        wire.setFace(BlockFace.WEST,  w);
    }

    private static int turnsFor(int dir) {
        return switch (dir) {
            case 0 -> 2; // SOUTH (180°)
            case 1 -> 3; // WEST (270°)
            case 3 -> 1; // EAST (90°)
            default -> 0; // NORTH (0°)
        };
    }

    private static BlockFace rotateFaceClockwise(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST  -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST  -> BlockFace.NORTH;
            default    -> face;
        };
    }
}
