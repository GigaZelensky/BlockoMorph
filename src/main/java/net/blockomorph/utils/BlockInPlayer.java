package net.blockomorph.utils;

import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInPlayer {
    private final BlockState blockState;
    private final BlockBracker blockBracker;
    private final UseController useController;

    public BlockInPlayer(BlockState state, BlockBracker bracker, UseController controller) {
        this.blockState = state;
        this.blockBracker = bracker;
        this.useController = controller;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public BlockBracker getBlockBracker() {
        return this.blockBracker;
    }

    public UseController getUseController() {
        return this.useController;
    }

    public void tick() {
        this.blockBracker.tick();
        this.useController.tick();
    }

    public void animateTick() {
        if (useController.getOwner().level().isClientSide) {
            BlockPos pos = BlockPos.containing(this.useController.getRealPos());
            this.blockState.getBlock().animateTick(
                    this.blockState,
                    this.useController.getTickingLevel(),
                    BlockPosAccessor.of(pos).setUseController(this.useController),
                    this.useController.getOwner().getRandom()
            );
        }
    }

}
