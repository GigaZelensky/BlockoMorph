package net.blockomorph.mixins.main.server;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPacketListenerMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "isPlayerCollidingWithAnythingNew", at = @At("HEAD"), cancellable = true) //TODO
    public void checkCollision(LevelReader levelReader, AABB playerBox, double moveX, double moveY, double moveZ, CallbackInfoReturnable<Boolean> cir) {
    	ServerPlayer player = ((ServerGamePacketListenerImpl) (Object)this).player;
    	
        AABB movedBox = player.getBoundingBox().move(moveX - player.getX(), moveY - player.getY(), moveZ - player.getZ());
        Iterable<VoxelShape> collisions = levelReader.getCollisions(player, movedBox.deflate(1.0E-5F));

        boolean isAlreadyInsideShape = false;
        boolean isTryingToEnterShape = false;

        for (VoxelShape collisionShape : collisions) {
            if (Shapes.joinIsNotEmpty(Shapes.create(playerBox.deflate(1.0E-5F)), collisionShape, BooleanOp.AND)) {
                isAlreadyInsideShape = true;
            }

            if (Shapes.joinIsNotEmpty(Shapes.create(movedBox.deflate(1.0E-5F)), collisionShape, BooleanOp.AND)) {
                isTryingToEnterShape = true;
            }
        }

        if (isTryingToEnterShape && !isAlreadyInsideShape) {
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"), cancellable = true)
    private void redirectUpdate(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        BlockHitResult res = packet.getHitResult();
        BlockPos pos = res.getBlockPos().relative(res.getDirection());
        InPlayerBlockPos.check(pos, (pl, realPos) -> {
            ci.cancel();
            this.player.connection.send(new ClientboundBlockUpdatePacket(pos, pl.getBlockState(realPos)));
        }, null, this.player.serverLevel());
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"), cancellable = true)
    public void checkAccess(ServerboundUseItemOnPacket pkt, CallbackInfo ci) {
        BlockHitResult hit = pkt.getHitResult();
        InPlayerBlockPos.check(hit.getBlockPos(), (pl, realPos) -> {
            if (!pl.isActive()) {
                ci.cancel();
            }
        }, null, this.player.level());
        if (!ci.isCancelled() && MorphUtils.needRejectUse(this.player.level(), hit)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    public void checkAccess(ServerboundInteractPacket pkt, CallbackInfo ci) {
        Entity entity = pkt.getTarget(this.player.serverLevel());
        if (entity instanceof PlayerAccessor pl && pl.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V"), cancellable = true)
    public void checkAccess(ServerboundPlayerActionPacket pkt, CallbackInfo ci) {
        switch (pkt.getAction()) {
            case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK: {
                InPlayerBlockPos.check(pkt.getPos(), (pl, realPos) -> {
                    if (!pl.isFullActive()) {
                        ci.cancel();
                    }
                }, null, this.player.level());
            }
            default:
		}
    }


    @Unique
    private Vec3 tpPos;

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At(value = "HEAD"))
    public void redirect(double x, double y, double z, float yaw, float xRot, Set<RelativeMovement> p_9786_, CallbackInfo ci) {
        this.tpPos = InPlayerBlockPos.checkOnReal(new Vec3(x, y, z));
    }

    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;)V", at = @At(value = "HEAD"), ordinal = 0)
    public double getX(double value) {
        return tpPos.x;
    }

    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;)V", at = @At(value = "HEAD"), ordinal = 1)
    public double getY(double value) {
        return tpPos.y;
    }

    @ModifyVariable(method = "teleport(DDDFFLjava/util/Set;)V", at = @At(value = "HEAD"), ordinal = 2)
    public double getZ(double value) {
        return tpPos.z;
    }
}
