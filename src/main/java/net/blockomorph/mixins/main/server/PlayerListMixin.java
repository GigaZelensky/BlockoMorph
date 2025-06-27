package net.blockomorph.mixins.main.server;

import net.blockomorph.network.ClientBoundBlockPosBoundPacket;
import net.blockomorph.network.ClientBoundMorphUpdatePacket;
import net.blockomorph.utils.coords.BlockPosBounds;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.PlayerMorphedSection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Debug(export = true)
@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Unique private Vec3 tempPos;

	@Inject(method = "broadcast", at = @At(value = "HEAD"))
	public void redirect(Player p_11242_, double x, double y, double z, double p_11246_, ResourceKey<Level> p_11247_, Packet<?> p_11248_, CallbackInfo ci) {
		this.tempPos = InPlayerBlockPos.checkOnReal(new Vec3(x, y, z));
	}

	@ModifyVariable(method = "broadcast", at = @At(value = "HEAD"), ordinal = 0)
	public double getX(double value) {
		return tempPos.x;
	}

	@ModifyVariable(method = "broadcast", at = @At(value = "HEAD"), ordinal = 1)
	public double getY(double value) {
		return tempPos.y;
	}

	@ModifyVariable(method = "broadcast", at = @At(value = "HEAD"), ordinal = 2)
	public double getZ(double value) {
		return tempPos.z;
	}


	@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void respawnPlayer(ServerPlayer oldPlayer, boolean p_11238_, CallbackInfoReturnable<ServerPlayer> cir, BlockPos blockpos, float f, boolean flag, ServerLevel serverlevel, Optional<Vec3> optional, ServerLevel serverlevel1, ServerPlayer newPlayer) {
		BlockPosBounds.onJoin(newPlayer);
		PlayerAccessor pl = PlayerAccessor.of(newPlayer);
		MorphUtils.sendPlayer(new ClientBoundMorphUpdatePacket(pl), newPlayer);
	}

	@Inject(method = "sendAllPlayerInfo", at = @At(value = "TAIL"))
	public void sendBoundedData(ServerPlayer player, CallbackInfo ci) {
		BlockPosBounds.onJoin(player);
		PlayerAccessor pl = PlayerAccessor.of(player);
		MorphUtils.sendPlayer(new ClientBoundMorphUpdatePacket(pl), player);
	}

	@Inject(method = "remove", at = @At("HEAD"))
	public void disconnectPlayer(ServerPlayer player, CallbackInfo ci) {
		if (player.isPassenger() && player.getVehicle() instanceof ServerPlayer) {
			player.stopRiding();
		}
	}

	@Inject(method = "placeNewPlayer", at = @At(shift = At.Shift.BEFORE, value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;"))
	public void boundBlockPos(Connection connection, ServerPlayer player, CallbackInfo ci) {
		BlockPosBounds.boundPlayer(player);
	}

	@Inject(method = "placeNewPlayer", at = @At(shift = At.Shift.BEFORE, value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addNewPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
	public void sendBlockPos(Connection connection, ServerPlayer player, CallbackInfo ci) {
		PlayerMorphedSection pos = BlockPosBounds.getChunkPosForPlayer(player);
		if (pos != null) {
			MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(pos, player, false), player);
		}
	}

	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	public void sendPrimaryData(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
		MorphUtils.onJoin(serverPlayer);
	}

}
