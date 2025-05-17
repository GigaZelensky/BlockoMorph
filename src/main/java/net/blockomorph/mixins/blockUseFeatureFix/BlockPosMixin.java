package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;
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

    @Inject(method = "relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(Direction p_121946_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "relative(Lnet/minecraft/core/Direction;I)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(Direction p_121948_, int p_121949_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "relative(Lnet/minecraft/core/Direction$Axis;I)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(Direction.Axis p_121943_, int p_121944_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "offset(III)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(int par1, int par2, int par3, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "offset(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(Vec3i p_121956_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "multiply(I)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr(int p_175263_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "subtract(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr2(Vec3i p_121997_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "cross(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr3(Vec3i p_121997_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "rotate", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr3(Rotation p_121918_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Inject(method = "atY", at = @At(value = "RETURN"), cancellable = true)
    public void transportCtr2(int p_175289_, CallbackInfoReturnable<BlockPos> cir) {
        this.doTransfer(cir);
    }

    @Unique
    private void doTransfer(CallbackInfoReturnable<BlockPos> cir) {
        if (this.controller != null) {
            UseController ctr = this.controller.getPl().getUseControllers().get(cir.getReturnValue());
            cir.setReturnValue(BlockPosAccessor.of(cir.getReturnValue()).setUseController(ctr));
        }
    }
}
