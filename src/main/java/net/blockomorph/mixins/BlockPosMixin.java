package net.blockomorph.mixins;

import net.blockomorph.utils.BlockPosBounds;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin implements BlockPosAccessor {
    @Unique
    UseController controller;

    public BlockPos setUseController(UseController ctr) {
        BlockPos that = (BlockPos)((Object) this);
        if (that == BlockPos.ZERO) {
            return BlockPosAccessor.of(new BlockPos(0, 0, 0)).setUseController(ctr);
        }
        this.controller = ctr;
        return that;
    }

    public UseController getController() {
        return this.controller;
    }

    @Override
    public String toString() {
        if (this.controller != null) {
            BlockPos pos = this.controller.getOffset();
            return "Morphed player " + this.controller.getOwner() + " bounded blockpos, position in local coordinate system: " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        }
        return super.toString();
    }

    @Inject(method = "*", at = @At(value = "RETURN"), cancellable = true)
    public <T> void transportCtr(CallbackInfoReturnable<T> cir) {
        T val = cir.getReturnValue();
        if (this.controller != null && val instanceof BlockPos pos) {
            UseController ctr = this.controller.getPl().getUseControllers().get(pos);
            cir.setReturnValue((T) BlockPosAccessor.of(pos).setUseController(ctr));
        }
    }
}
