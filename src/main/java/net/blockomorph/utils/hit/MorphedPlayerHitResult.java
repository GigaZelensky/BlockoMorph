package net.blockomorph.utils.hit;

import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MorphedPlayerHitResult extends BlockHitResult {
    private final PlayerAccessor player;
    private final Vec3 inBlockOffset;

    protected MorphedPlayerHitResult(PlayerAccessor player, Vec3 location, Vec3 inBlockOffset, BlockPos offset, Direction direction, boolean inside) {
        super(location, direction, offset, inside);
        this.player = player;
        this.inBlockOffset = inBlockOffset;
    }

    @Override
    public @NotNull Type getType() {
        return Type.BLOCK;
    }

    public PlayerAccessor getPlayer() {
        return player;
    }

    public Vec3 getInBlockOffset() {
        return inBlockOffset;
    }

    public BlockPos getOffset() {
        return this.getBlockPos();
    }

    public MorphedPlayerHitResult boundBlockPos() {
        return new MorphedPlayerHitResult(this.player, this.getLocation(), this.inBlockOffset, player.getUseControllers().get(this.getOffset()).getOffset(), this.getDirection(), this.isInside());
    }
}
