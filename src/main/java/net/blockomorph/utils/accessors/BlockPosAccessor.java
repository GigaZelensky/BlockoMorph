package net.blockomorph.utils.accessors;

import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;

public interface BlockPosAccessor {
    BlockPos setUseController(UseController ctr);
    UseController getController();

    static BlockPosAccessor of(BlockPos pos) {
        return (BlockPosAccessor) pos;
    }
}
