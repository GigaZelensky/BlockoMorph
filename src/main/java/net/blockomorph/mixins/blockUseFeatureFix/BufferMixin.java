package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(FriendlyByteBuf.class)
public class BufferMixin {

    @Inject(method = "writeBlockPos", at = @At(value = "RETURN"))
    public void writePos(BlockPos pos, CallbackInfoReturnable<FriendlyByteBuf> cir) {
        FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
        if (!MorphUtils.doActionFromBounedBlockPos(pos, (ctr, realPos) -> {
            buf.writeOptional(Optional.of(ctr.getOwner().getUUID()), FriendlyByteBuf::writeUUID);
            buf.writeLong(realPos.asLong());
        })) {
            buf.writeOptional(Optional.empty(), FriendlyByteBuf::writeUUID);
        }
    }

    @Inject(method = "readBlockPos", at = @At(value = "RETURN"), cancellable = true)
    public void readPos(CallbackInfoReturnable<BlockPos> cir) {
        FriendlyByteBuf buf = (FriendlyByteBuf) (Object) this;
        Optional<UUID> uuid = buf.readOptional(FriendlyByteBuf::readUUID);
        if (uuid.isPresent()) {
            UUID id = uuid.get();
            BlockPos offset = BlockPos.of(buf.readLong());
            UseController ctr = MorphUtils.getControllerFromNetwork(id, offset);
            cir.setReturnValue(BlockPosAccessor.of(offset).setUseController(ctr));
        }
    }
}
