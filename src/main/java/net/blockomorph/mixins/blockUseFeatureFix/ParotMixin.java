package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParotMixin extends LivingEntity {
    UseController controller;

    protected ParotMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(method = "setRecordPlayingNearby", at = @At(value = "HEAD"), cancellable = true)
    public void setRecordPlayingNearby(BlockPos pos, boolean act, CallbackInfo ci) {
        MorphUtils.doActionFromBounedBlockPos(pos, (ctr, rP) -> {
            ci.cancel();
            this.controller = ctr;
        });
    }

    @Inject(method = "aiStep", at = @At(value = "TAIL"))
    public void tick(CallbackInfo ci) {
        if (controller != null && (!controller.isValid() || controller.getOwner().distanceToSqr(this.position()) > Mth.square(3.46))) {
            controller = null;
        }
    }

    @Inject(method = "isPartyParrot", at = @At(value = "HEAD"), cancellable = true)
    public void fix(CallbackInfoReturnable<Boolean> cir) {
        if (controller != null)
            cir.setReturnValue(true);
    }
}
