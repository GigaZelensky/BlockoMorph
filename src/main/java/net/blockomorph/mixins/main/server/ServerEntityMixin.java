package net.blockomorph.mixins.main.server;

import net.blockomorph.network.BlockMorphPacket;
import net.blockomorph.network.ClientBoundBlockPosBoundPacket;
import net.blockomorph.network.ClientBoundMorphUpdatePacket;
import net.blockomorph.utils.coords.BlockPosBounds;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.PlayerMorphedSection;
import net.blockomorph.utils.dataSyncher.SyncedEntity;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {

	@Shadow @Final private Entity entity;
    @Shadow @Final private ServerLevel level;

    @Inject(method = "sendChanges", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER))
	private void fixAsync(CallbackInfo ci) {
		if (this.entity instanceof ServerPlayer pl) {
			pl.connection.send(new ClientboundSetPassengersPacket(pl));
		}
	}

	@Inject(method = "addPairing", at = @At(value = "TAIL"))
	public void start(ServerPlayer looker, CallbackInfo ci) {
		if (this.entity instanceof ServerPlayer pl && this.entity instanceof PlayerAccessor acc) {
			PlayerMorphedSection pos = BlockPosBounds.getChunkPosForPlayer(pl);
			if (pos != null) {
				MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(pos, pl, false), looker);
			}
			MorphUtils.sendPlayer(new ClientBoundMorphUpdatePacket(acc), looker);
		}
        SyncedEntity.of(this.entity).checkOrSendImmediate(this::sendCustomPacket, true);
	}


    @Inject(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPassengers()Ljava/util/List;"))
    public void sync(CallbackInfo ci) {
        SyncedEntity.of(this.entity).checkOrSendImmediate(this::sendCustomPacket, false);
    }

	@Inject(method = "removePairing", at = @At(value = "TAIL"))
	public void stop(ServerPlayer looker, CallbackInfo ci) {
		if (this.entity instanceof ServerPlayer pl) {
			PlayerMorphedSection pos = BlockPosBounds.getChunkPosForPlayer(pl);
			if (pos != null) {
				MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(pos, pl, true), looker);
			}
		}
	}


    private void sendCustomPacket(BlockMorphPacket packet) {
        this.level.getChunkSource().chunkMap.getPlayers(this.entity.chunkPosition(), false).forEach(player -> {
            MorphUtils.sendPlayer(packet, player);
        });
    }
}
