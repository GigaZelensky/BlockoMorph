package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.MorphUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {

    @Shadow public abstract void broadcastAndSend(Entity p_8395_, Packet<?> p_8396_);

    @Inject(method = "blockChanged", at = @At(value = "HEAD"), cancellable = true)
    public void redirectChange(BlockPos pos, CallbackInfo ci) {
        MorphUtils.doActionFromBounedBlockPos(pos, (ctr, bp) -> {
            ci.cancel();
            BlockEntity ent = ctr.getBlockEntity();
            if (ent != null && ent.getUpdatePacket() != null) {
                this.broadcastAndSend(ctr.getOwner(), ent.getUpdatePacket());
            }
        });
    }
}
