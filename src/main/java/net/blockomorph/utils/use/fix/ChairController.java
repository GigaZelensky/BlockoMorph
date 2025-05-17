package net.blockomorph.utils.use.fix;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.use.UseController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ChairController {
    public final SynchedEntityData entityData;
    public final EntityDataAccessor<CompoundTag> TARGET;
    private final Player owner;
    private final PlayerAccessor ownPl;
    private Vec3 pos;
    private UseController target;

    public ChairController(PlayerAccessor playerAccessor, SynchedEntityData data, EntityDataAccessor<CompoundTag> sync) {
        this.owner = Objects.requireNonNull(playerAccessor).player();
        this.entityData = data;
        this.TARGET = sync;
        this.ownPl = playerAccessor;
    }

    public Vec3 getChairPos() {
        return this.pos;
    }

    public boolean isWorking() {
        return this.target != null && this.owner.getVehicle() == this.target.getOwner() && this.target.isValid();
    }

    public UseController getTarget() {
        return this.target;
    }

    public void tick() {
        if (!this.owner.level().isClientSide && this.target != null) {
            boolean flag = this.owner.getVehicle() != this.target.getOwner();
            if (flag || !this.target.isValid()) {
                this.setSittingBound(null, null, !flag);
            }
        }
    }

    public void setSittingBound(UseController target, Entity seatEntityFromOtherMod, boolean needEjecct) {
        if (this.owner.level().isClientSide) return;
        CompoundTag tg = new CompoundTag();
        if (target != null && seatEntityFromOtherMod != null) {
            Vec3 pos = this.getSeatEntityOffset(seatEntityFromOtherMod);
            seatEntityFromOtherMod.discard();
            Player player = target.getOwner();
            CompoundTag position = new CompoundTag();
            position.putDouble("x", pos.x);
            position.putDouble("y", pos.y);
            position.putDouble("z", pos.z);
            tg.put("pos", position);
            tg.putInt("id", player.getId());
            tg.putString("innerPos", MorphUtils.getBlockPos(target.getOffset()));
            if (ownPl.getBedController().isWorking()) {
                ownPl.getBedController().stopSleep(false);
            }
            if (this.isWorking()) {
                this.setSittingBound(null, null, false);
            }
            this.owner.startRiding(player, true);
        } else {
            if (needEjecct) this.owner.stopRiding();
            if (!this.owner.level().isClientSide && this.target != null) {
                BlockState chair = this.target.getBlockState();
                chair = this.findOccupied(chair);
                this.target.getPl().enableBlockOverrides(SavedBlock.getSetBlockMap(this.target.getOffset(), chair));
            }
        }
        this.entityData.set(TARGET, tg);
    }

    private BlockState findOccupied(BlockState state) {
        for (Property<?> prop : state.getProperties()) {
            if (prop instanceof BooleanProperty bProp && bProp.getName().equalsIgnoreCase("occupied")) {
                return state.setValue(bProp, false);
            }
        }
        return state;
    }

    private Vec3 getSeatEntityOffset(Entity entity) {
        double y = entity.getY() + entity.getPassengersRidingOffset() + this.owner.getMyRidingOffset();
        return new Vec3(entity.getX(), y, entity.getZ());
    }

    public void syncData() {
        CompoundTag tg = this.entityData.get(TARGET);
        if (!tg.isEmpty()) {
            int id = tg.getInt("id");
            CompoundTag pos = tg.getCompound("pos");
            if (this.owner.level().getEntity(id) instanceof PlayerAccessor playerAccessor) {
                this.target = playerAccessor.getUseControllers().get(MorphUtils.parseBlockPos(tg.getString("innerPos")));
                this.pos = new Vec3(pos.getDouble("x"), pos.getDouble("y"), pos.getDouble("z"));
            }
        } else {
            this.target = null;
            this.pos = null;
        }
    }
}
