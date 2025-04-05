package net.blockomorph.utils.use.fix;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.use.UseController;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class BedController {
    public final SynchedEntityData entityData;
    public final EntityDataAccessor<CompoundTag> TARGET;
    private final Player owner;
    private UseController target;
    private int sleepCounter;
    private boolean inited;

    public BedController(PlayerAccessor playerAccessor, SynchedEntityData data, EntityDataAccessor<CompoundTag> sync) {
        this.owner = ((Player)Objects.requireNonNull(playerAccessor));
        this.entityData = data;
        this.TARGET = sync;
    }

    public int getSleepCounter() {
        return this.sleepCounter;
    }

    public void tick() {
        if (!this.owner.level().isClientSide && target != null) {
            boolean flag = this.owner.getVehicle() != this.target.getOwner();
            if (flag || !this.target.isValid())
                this.stopSleep(!flag);
        }
        if (this.isWorking()) {
            this.sleepCounter++;
            if (this.sleepCounter > 100) {
                this.sleepCounter = 100;
            }
            if (!this.owner.level().isClientSide && this.owner.level().isDay()) {
                this.stopSleep(true);
            }
        } else if (this.sleepCounter > 0) {
            this.sleepCounter++;
            if (this.sleepCounter >= 110) {
                this.sleepCounter = 0;
            } else if (this.sleepCounter < 100) {
                this.sleepCounter = 0;
            }
        }
    }

    public boolean isWorking() {
        return this.target != null && this.owner.getVehicle() == this.target.getOwner() && this.target.isValid();
    }

    public InteractionResult clckBed(PlayerAccessor clickedPlayer, BlockHitResult hiter) {
        UseController ctr = clickedPlayer.getUseControllers().get(hiter.getBlockPos());
        if (ctr != null && !this.owner.level().isClientSide) {
            BlockState st = ctr.getBlockState();
            if (st.getBlock() instanceof BedBlock) {
                BlockState head = st;
                if (st.getValue(BedBlock.PART) == BedPart.FOOT) {
                    BlockPos offseted = ctr.getOffset().relative(st.getValue(FACING));
                    UseController ctr2 = clickedPlayer.getUseControllers().get(offseted);
                    if (ctr2 != null) {
                        head = ctr2.getBlockState();
                        ctr = ctr2;
                    }
                }
                if (head.getValue(BedBlock.PART) == BedPart.HEAD) {
                    if (!this.owner.level().dimensionType().bedWorks()) {
                        MorphUtils.destroy(clickedPlayer, owner);
                        this.owner.level().explode(null, this.owner.level().damageSources().badRespawnPointExplosion(ctr.getRealPos()), null, ctr.getRealPos(), 5.0F, true, Level.ExplosionInteraction.BLOCK);
                    } else if (head.getValue(BedBlock.OCCUPIED)) {
                        owner.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
                    } else {
                        this.startSleep(ctr);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.CONSUME;
    }

    public void startSleep(UseController ctr) {
        if (ctr != null && ctr.getBlockState().getBlock() instanceof BedBlock && ctr.isValid() && this.getTarget() == null && this.owner.isAlive() && owner instanceof ServerPlayer pla) {
            Direction direction = ctr.getBlockState().getValue(FACING);
            if (!this.owner.level().dimensionType().natural()) {
                this.sendProblemMessage(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
            } else if (!this.bedInRange(ctr, direction)) {
                this.sendProblemMessage(Player.BedSleepingProblem.TOO_FAR_AWAY);
            } else if (this.isBedBlocked(ctr, direction)) {
                this.sendProblemMessage(Player.BedSleepingProblem.OBSTRUCTED);
            } else {
                pla.setRespawnPosition(this.owner.level().dimension(), BlockPos.containing(ctr.getRealPos()), pla.getYRot(), false, true);
                if (this.owner.level().isDay()) {
                    this.sendProblemMessage(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
                } else {
                    if (!owner.isCreative()) {
                        double d0 = 8.0D;
                        double d1 = 5.0D;
                        Vec3 vec3 = ctr.getRealPos();
                        List<Monster> list = this.owner.level().getEntitiesOfClass(Monster.class, new AABB(vec3.x() - d0, vec3.y() - d1, vec3.z() - d0, vec3.x() + d0, vec3.y() + d1, vec3.z() + d0), (p_9062_) -> p_9062_.isPreventingPlayerRest(pla));
                        if (!list.isEmpty()) {
                            this.sendProblemMessage(Player.BedSleepingProblem.NOT_SAFE);
                            return;
                        }

                    }
                    owner.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                    /*
                     *             !!!MAIN BUS LOGIC!!!
                     */
                    this.lanchSleep(ctr);
                    /////
                    owner.awardStat(Stats.SLEEP_IN_BED);
                    CriteriaTriggers.SLEPT_IN_BED.trigger(pla);
                    if (!pla.serverLevel().canSleepThroughNights()) {
                        owner.displayClientMessage(Component.translatable("sleep.not_possible"), true);
                    }
                }
            }
        } else {
            this.sendProblemMessage(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    private boolean isBedBlocked(UseController ctr, Direction direction) {
        UseController one = ctr.getPl().getUseControllers().get(ctr.getOffset());
        UseController two = ctr.getPl().getUseControllers().get(ctr.getOffset().relative(direction.getOpposite()));
        boolean bl1 = false;
        boolean bl2 = false;
        if (one != null) {
            BlockPos realBlockInWorld = BlockPos.containing(one.getRealPos().add(0, 1, 0));
            bl1 = this.owner.level().getBlockState(realBlockInWorld).isSuffocating(this.owner.level(), realBlockInWorld);
        }
        if (two != null) {
            BlockPos realBlockInWorld = BlockPos.containing(two.getRealPos().add(0, 1, 0));
            bl2 = this.owner.level().getBlockState(realBlockInWorld).isSuffocating(this.owner.level(), realBlockInWorld);
        }
        return bl1 || bl2;
    }

    private boolean bedInRange(UseController ctr, Direction direction) {
        UseController one = ctr.getPl().getUseControllers().get(ctr.getOffset());
        UseController two = ctr.getPl().getUseControllers().get(ctr.getOffset().relative(direction.getOpposite()));
        boolean flag = false;
        boolean flag2 = false;
        if (one != null) {
            Vec3 vec3 = one.getRealPos();
            flag = Math.abs(owner.getX() - vec3.x()) <= 3.0D && Math.abs(owner.getY() - vec3.y()) <= 2.0D && Math.abs(owner.getZ() - vec3.z()) <= 3.0D;
        }
        if (two != null) {
            Vec3 vec3 = two.getRealPos();
            flag2 = Math.abs(owner.getX() - vec3.x()) <= 3.0D && Math.abs(owner.getY() - vec3.y()) <= 2.0D && Math.abs(owner.getZ() - vec3.z()) <= 3.0D;
        }
        return flag || flag2;
    }

    private void sendProblemMessage(Player.BedSleepingProblem problem) {
        if (problem.getMessage() != null) {
            this.owner.displayClientMessage(problem.getMessage(), true);
        }
    }

    public void stopSleep(boolean needEject) {
        UseController old = this.target;
        this.entityData.set(TARGET, new CompoundTag());
        if (needEject)
            this.owner.stopRiding();
        if (this.owner.level() instanceof ServerLevel lv)
            lv.updateSleepingPlayerList();
        this.changeAccupied(old, false);
    }

    private void lanchSleep(UseController ctr) {
        this.sleepCounter = 0;
        if (owner.isPassenger()) {
            owner.stopRiding();
        }
        this.changeAccupied(ctr, true);
        this.setBound(ctr);//bad logic//нажмите шифт чтобы спешиться
        this.owner.startRiding(ctr.getOwner(), true);
        if (this.owner.level() instanceof ServerLevel lv)
            lv.updateSleepingPlayerList();
    }

    private void changeAccupied(UseController bed, boolean yes) {
        HashMap<BlockPos, SavedBlock> map = new HashMap<>();
        map.put(bed.getOffset(), new SavedBlock(bed.getBlockState().setValue(BedBlock.OCCUPIED, yes), new CompoundTag(), ""));
        bed.getPl().enableBlockOverrides(map);
    }

    private void setBound(UseController ctr) {
        CompoundTag tg = new CompoundTag();
        tg.putInt("id", ctr.getOwner().getId());
        tg.putLong("offset", ctr.getOffset().asLong());
        this.entityData.set(TARGET, tg);
    }

    public void syncData() {
        CompoundTag tg = this.entityData.get(TARGET);
        if (!tg.isEmpty()) {
            int id = tg.getInt("id");
            BlockPos offset = BlockPos.of(tg.getLong("offset"));
            if (this.owner.level().getEntity(id) instanceof PlayerAccessor playerAccessor) {
                this.target = playerAccessor.getUseControllers().get(offset);
                this.inited = true;
            }
        } else {
            this.target = null;
            if (this.inited)
                this.sleepCounter = 100;
        }
    }

    @Nullable
    public UseController getTarget() {
        return this.target;
    }
}
