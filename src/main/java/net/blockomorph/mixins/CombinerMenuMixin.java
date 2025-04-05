package net.blockomorph.mixins;

import net.blockomorph.utils.accessors.MenuAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemCombinerMenu.class)
public abstract class CombinerMenuMixin {

    @Inject(method = "stillValid", at = @At(value = "HEAD"), cancellable = true)
    public void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player.containerMenu instanceof MenuAccessor accessor) {
            UseController ctr = accessor.getPlayer();
            if (ctr != null) {
                if (!ctr.isValid()) {
                    cir.setReturnValue(false);
                } else {
                    if (!(player.distanceToSqr(ctr.getRealPos()) <= 64.0D)) {
                        cir.setReturnValue(false);
                    } else cir.setReturnValue(true);
                }
            }
        }
    }
}
