package net.blockomorph.mixins.test;

import net.blockomorph.core.Test;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Test.class)
public class TestMixin {

    /*@Redirect(method = "get", at = @At(target = "Lnet/blockomorph/core/Test;defaultM()Z", value = "INVOKE", remap = false), remap = false)
    private static boolean test() {
        return true;
    }

    @Redirect(method = "get", at = @At(target = "Lnet/blockomorph/core/Test;defaultM()Z", value = "INVOKE", remap = false), remap = false, require = 1)
    private static boolean test2() {
        return false;
    }*/

    @Inject(method = "get", at = @At(value = "RETURN"), remap = false,cancellable = true)
    private static void te(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            cir.setReturnValue(Test.op1());
    }

    @Inject(method = "get", at = @At(value = "RETURN"), remap = false,cancellable = true)
    private static void te2(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue())
            cir.setReturnValue(Test.op2());
    }

}
