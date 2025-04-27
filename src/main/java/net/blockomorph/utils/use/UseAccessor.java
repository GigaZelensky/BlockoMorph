package net.blockomorph.utils.use;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

public interface UseAccessor {
    HashMap<BlockPos, BlockState> getBlocks();
    Level getRealLevel();
    void setRealBlockPosMode(boolean yes);
    boolean isRealPosMode();
    UseController getController();

    default BlockPos calculateRealPos(BlockPos blockPos) {
        if (blockPos instanceof BlockPosAccessor acc && acc.getController() != null) {
            return acc.getController().getOffset();
        }
        BlockPos rp = BlockPos.containing(this.getController().getRealPos());
        if (this.isRealPosMode() || blockPos.equals(rp)) {
            BlockPos rp2 = BlockPos.containing(MorphUtils.getCetneredRealBlockPos(this.getController().getPl(), BlockPos.ZERO));
            int x = blockPos.getX() - rp2.getX();
            int y = blockPos.getY() - rp2.getY();
            int z = blockPos.getZ() - rp2.getZ();
            return new BlockPos(x, y, z);
        }
        return blockPos;
    }
}
