package com.venomgrave.hexvg.schematic;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Stairs;


public class SchematicRotator {

    public static Schematic rotate(Schematic original, int direction) {
        if (direction == 2) return original;

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

                    int nx, nz;
                    switch (direction) {
                        case 0:
                            nx = srcWidth  - 1 - x;
                            nz = srcLength - 1 - z;
                            break;
                        case 1:
                            nx = z;
                            nz = srcWidth - 1 - x;
                            break;
                        case 3:
                            nx = srcLength - 1 - z;
                            nz = x;
                            break;
                        default:
                            nx = x; nz = z;
                    }

                    dst[y][nz][nx] = rotateBlock(sb, direction);
                }
            }
        }

        String suffix = new String[]{"_S","_W","_N","_E"}[direction];
        return new Schematic(
                original.getName() + suffix,
                dstWidth, srcHeight, dstLength,
                original.getLootMin(), original.getLootMax(),
                dst
        );
    }


    private static SchematicBlock rotateBlock(SchematicBlock sb, int dir) {
        if (sb == null || sb.isSkip() || sb.getMaterial() == null) return sb;
        if (sb.getBlockDataString() == null) return sb; // no orientation to rotate

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

        if (bd instanceof Directional) {
            Directional d = (Directional) bd;
            for (int i = 0; i < turns; i++) {
                d.setFacing(rotateFaceClockwise(d.getFacing()));
            }
        }

        if (bd instanceof Rotatable) {
            Rotatable r = (Rotatable) bd;
            for (int i = 0; i < turns; i++) {
                r.setRotation(rotateFaceClockwise(r.getRotation()));
            }
        }

        if (bd instanceof Stairs) {
        }
    }


    private static int turnsFor(int dir) {
        switch (dir) {
            case 0: return 2; // SOUTH = 180 deg
            case 1: return 3; // WEST  = 270 deg
            case 3: return 1; // EAST  =  90 deg
            default: return 0;
        }
    }

    private static BlockFace rotateFaceClockwise(BlockFace face) {
        switch (face) {
            case NORTH: return BlockFace.EAST;
            case EAST:  return BlockFace.SOUTH;
            case SOUTH: return BlockFace.WEST;
            case WEST:  return BlockFace.NORTH;
            case UP:    return BlockFace.UP;
            case DOWN:  return BlockFace.DOWN;
            default:    return face;
        }
    }
}