package net.blockomorph.mixins;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.BlockEntityAccessor;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements BlockEntityAccessor {
    @Unique
    UseController controller;

    @Inject(method = "getBlockPos", at = @At(value = "HEAD"), cancellable = true)
    public void getPos(CallbackInfoReturnable<BlockPos> cir) {
        if (this.controller != null) {
            BlockPos pos = BlockPos.containing(this.controller.getRealPos());
            cir.setReturnValue(BlockPosAccessor.of(pos).setUseController(this.controller));
        }
    }

    public void setUseController(UseController ctr) {
        this.controller = ctr;
    }

    public UseController getController() {
        return this.controller;
    }

    @Inject(method = "saveMetadata", at = @At(value = "TAIL"))
    public void saveController(CompoundTag tag, CallbackInfo ci) {
        if (this.controller != null) {
            CompoundTag tg = new CompoundTag();
            tg.putUUID("owner", this.controller.getOwner().getUUID());
            tg.putLong("offset", this.controller.getOffset().asLong());
            tag.put("BlockoMorph", tg);
        }
    }

    @Inject(method = "getPosFromTag", at = @At(value = "RETURN"), cancellable = true)
    private static void createPos(CompoundTag tg, CallbackInfoReturnable<BlockPos> cir) {
        if (tg.contains("BlockoMorph")) {
            CompoundTag tg2 = tg.getCompound("BlockoMorph");
            UseController ctr = MorphUtils.getControllerFromNetwork(tg2.getUUID("owner"), BlockPos.of(tg2.getLong("offset")));
            if (ctr != null)
                cir.setReturnValue(ctr.getOffset());
        }
    }
}
