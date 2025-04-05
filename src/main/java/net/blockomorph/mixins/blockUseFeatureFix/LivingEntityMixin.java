package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.use.fix.BedController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "checkBedExists", at = @At(value = "HEAD"), cancellable = true)
    public void fix(CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof PlayerAccessor playerAccessor) {
            BedController bd = playerAccessor.getBedController();
            if (bd.isWorking() && bd.getTarget().getBlockState().getBlock() instanceof BedBlock) {
                cir.setReturnValue(true);
            }
        }
    }
}
