package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "HEAD"))
    public void fixRiding(LivingEntity lv, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_, CallbackInfo ci) {
        this.fix(lv, humanoidModel -> humanoidModel.riding = false);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    public void fixRiding2(LivingEntity lv, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_, CallbackInfo ci) {
        this.fix(lv, humanoidModel -> humanoidModel.head.yRot = 0);
    }

    @Unique
    private void fix(LivingEntity lv, Consumer<HumanoidModel<?>> cons) {
        if (lv instanceof PlayerAccessor playerAccessor) {
            if (playerAccessor.getBedController().isWorking()) {
                cons.accept((HumanoidModel<?>)(Object)this);
            }
        }
    }

}
