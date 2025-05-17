package net.blockomorph.utils.use;

import net.blockomorph.network.blockFix.ClientBoundBlockEventPacket;
import net.blockomorph.utils.MultiBlockLevel;
import net.blockomorph.utils.accessors.EntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UseLevel extends MultiBlockLevel implements UseAccessor {
    protected final UseController useController;
    private boolean realBlockPosMode;
    private final ModelDataManager MODEL_DATA_MANAGER = new ModelDataManager(this);

    private UseLevel(Level lv, UseController ctr) {
        super(lv, true);
        this.useController = ctr;
    }

    public static Level getUseLevel(UseController ctr) {
        Player pl = ctr.getOwner();
        if (pl instanceof ServerPlayer pl2) {
            return new UseServerLevel(pl2.serverLevel(), ctr);
        }
        return new UseLevel(pl.level(), ctr);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos bl) {
        BlockPos pos = this.calculateRealPos(bl);
        UseController ctr = this.useController.getPl().getUseControllers().get(pos);
        if (ctr == null) return null;
        return ctr.getBlockEntity();
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
        BlockPos pos = this.calculateRealPos(blockPos);
        return super.setBlock(pos, blockState, i, j);
    }

    public void setRealBlockPosMode(boolean b) {
        this.realBlockPosMode = b;
    }

    @Override
    public boolean isRealPosMode() {
        return this.realBlockPosMode;
    }

    @Override
    public UseController getController() {
        return this.useController;
    }

    @Override
    public @NotNull List<? extends Player> players() {
        ArrayList<? extends Player> pls = new ArrayList<>(realLevel.players());
        pls.remove(this.useController.getOwner());
        return pls;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return UseAccessor.getState(this, blockPos);
    }

    @Override
    public void blockEntityChanged(BlockPos p_151544_) {
        //this.useController.checkChanges();
    }

    @Override
    public boolean addFreshEntity(Entity ent) {
        ((EntityAccessor)ent).forceLevelChange(realLevel);
        return realLevel.addFreshEntity(ent);
    }

    public void playSound(@Nullable Player pl, BlockPos p_46561_, SoundEvent p_46562_, SoundSource p_46563_, float p_46564_, float p_46565_) {
        Player owner = this.useController.getOwner();
        p_46561_ = this.calculateRealPos(p_46561_);
        realLevel.playSound(pl,
                (double)p_46561_.getX() + 0.5D + owner.getX(),
                (double)p_46561_.getY() + 0.5D + owner.getY(),
                (double)p_46561_.getZ() + 0.5D + owner.getZ(),
                p_46562_, p_46563_, p_46564_, p_46565_);
    }

    public void playLocalSound(BlockPos p_250938_, SoundEvent p_252209_, SoundSource p_249161_, float p_249980_, float p_250277_, boolean p_250151_) {
        Player owner = this.useController.getOwner();
        p_250938_ = this.calculateRealPos(p_250938_);
        realLevel.playLocalSound(
                (double)p_250938_.getX() + 0.5D + owner.getX(),
                (double)p_250938_.getY() + 0.5D + owner.getY(),
                (double)p_250938_.getZ() + 0.5D + owner.getZ(),
                p_252209_, p_249161_, p_249980_, p_250277_, p_250151_);
    }

    public void playSeededSound(@Nullable Player var1, double x, double y, double z, Holder<SoundEvent> var8, SoundSource var9, float var10, float var11, long var12) {
        Player owner = this.useController.getOwner();
        if (!this.realBlockPosMode && !(BlockPos.containing(x, y, z).equals(BlockPos.containing(this.useController.getRealPos())))) {
            x = x + owner.getX();
            y = y + owner.getY();
            z = z + owner.getZ();
        }
        //System.out.println(new Vec3(x, y, z));
        realLevel.playSeededSound(var1, x, y, z, var8, var9, var10, var11, var12);
    }

    public void blockEvent(BlockPos blockPos, Block block, int a, int b) {
        this.useController.getBlockState().triggerEvent(this, this.useController.getOffset(), a, b);
    }

    public void levelEvent(@Nullable Player player, int a, BlockPos blockPos, int b) {
        if (a == 1010) a = -2;
        if (a == 1011) a = -3;
        ClientBoundBlockEventPacket.levelEvent(this.useController.getOwner().getId(), this.useController.getOffset(), a, b).handle(null);
    }

    @Override
    public boolean noCollision(AABB input) {
        if (input.getCenter().distanceToSqr(this.useController.getOffset().getCenter()) < 64) {
            input = input.move(this.useController.getRealPos().add(-0.5, -0.5, -0.5));
        }
        return realLevel.noCollision(input);
    }

    public void addParticle(ParticleOptions args, double x, double y, double z, double dx, double dy, double dz) {
        this.recalculatePosForParticles(new Vec3(x, y, z), (pos) ->{
            this.realLevel.addParticle(args, pos.x, pos.y, pos.z, dx, dy, dz);
        });
    }

    public void addParticle(ParticleOptions args, boolean limit, double x, double y, double z, double dx, double dy, double dz) {
        this.recalculatePosForParticles(new Vec3(x, y, z), (pos) ->{
            this.realLevel.addParticle(args, limit, pos.x, pos.y, pos.z, dx, dy, dz);
        });
    }

    public void addAlwaysVisibleParticle(ParticleOptions args, double x, double y, double z, double dx, double dy, double dz) {
        this.recalculatePosForParticles(new Vec3(x, y, z), (pos) ->{
            this.realLevel.addAlwaysVisibleParticle(args, pos.x, pos.y, pos.z, dx, dy, dz);
        });
    }

    public void addAlwaysVisibleParticle(ParticleOptions args, boolean limit, double x, double y, double z, double dx, double dy, double dz) {
        this.recalculatePosForParticles(new Vec3(x, y, z), (pos) ->{
            this.realLevel.addAlwaysVisibleParticle(args, limit, pos.x, pos.y, pos.z, dx, dy, dz);
        });
    }

    @Override
    public net.minecraftforge.client.model.data.ModelDataManager getModelDataManager() {
        return MODEL_DATA_MANAGER;
    }
}
