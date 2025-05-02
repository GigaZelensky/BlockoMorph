package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.MorphUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "canRenderCrosshairForSpectator", at = @At(value = "HEAD"), cancellable = true)
    public void shouldRender(HitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (MorphUtils.hitEntity != null) {
            if (MorphUtils.canOpenMenuIn(MorphUtils.hitEntity, MorphUtils.hitPart)) {
                cir.setReturnValue(true);
            }
        }
    }
}
