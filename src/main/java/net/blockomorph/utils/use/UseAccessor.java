package net.blockomorph.utils.use;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

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

    static BlockState getState(UseAccessor acc, BlockPos blockPos) {
        BlockPos pos = acc.calculateRealPos(blockPos);
        if (acc.getBlocks().containsKey(pos)) return acc.getBlocks().get(pos);
        if (acc.isRealPosMode()) {
            if (pos.equals(acc.getController().getOffset())) {
                return acc.getController().getBlockState();
            } else {
                return acc.getRealLevel().getBlockState(blockPos);
            }
        }
        BlockState st = acc.getController().getPl().getBlocks().get(pos);
        if (st == null) return Blocks.AIR.defaultBlockState();
        return st;
    }

    default void recalculatePosForParticles(Vec3 pos, Consumer<Vec3> run) {
        run.accept(this.getParticlesPos(pos));
    }

    default <T> T recalculatePosForParticles(Vec3 pos, Function<Vec3, T> run) {
        return run.apply(this.getParticlesPos(pos));
    }

    private Vec3 getParticlesPos(Vec3 pos) {
        Vec3 realPos = this.getController().getRealPos();
        BlockPos offset = BlockPos.containing(realPos);
        realPos = realPos.add(-0.5, -0.5, -0.5);
        if (this.isRealPosMode()) {
            double x = pos.x - offset.getX();
            double y = pos.y - offset.getY();
            double z = pos.z - offset.getZ();
            return new Vec3(realPos.x + x, realPos.y + y, realPos.z + z);
        } else {
            return new Vec3(realPos.x + pos.x, realPos.y + pos.y, realPos.z + pos.z);
        }
    }
}
