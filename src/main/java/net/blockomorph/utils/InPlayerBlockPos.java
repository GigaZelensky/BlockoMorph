package net.blockomorph.utils;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public class InPlayerBlockPos {
    private static final int Y_CHUNK_START = Integer.MIN_VALUE + 2000;
    private static final int Y_CHUNK_END = Y_CHUNK_START - 325;
    public final int x;
    public final int y;
    public final int z;

    private InPlayerBlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static InPlayerBlockPos get(int x, int y, int z) {
        return new InPlayerBlockPos(x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof InPlayerBlockPos bl)
            return bl.x == this.x && bl.y == this.y && bl.z == this.z;
        return false;
    }

    @Nullable
    public static InPlayerBlockPos parseBlockPos(String input) {
        String[] parts = input.trim().split(" ");
        if (parts.length != 3)
            return null;
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new InPlayerBlockPos(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String string() {
        return this.x + " " + this.y + " " + this.z;
    }
}
