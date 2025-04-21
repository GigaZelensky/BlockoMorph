package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public class BufferMixin {

    @Inject(method = "writeBlockPos", at = @At(value = "RETURN"))
    public void writePos(BlockPos pos, CallbackInfoReturnable<FriendlyByteBuf> cir) {
        UseController ctr = MorphUtils.getControllerFromPos(pos);
        FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
        if (ctr != null) {
            buf.writeUUID(ctr.getOwner().getUUID());
            buf.writeLong(ctr.getOffset().asLong());
        }
    }

    @Inject(method = "readBlockPos", at = @At(value = "RETURN"))
    public void readPos(CallbackInfoReturnable<BlockPos> cir) {
        MinecraftServer sv = null;
    }
}
