package net.blockomorph.mixins.main.client;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.accessors.ScreenAccessor;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

	@Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;keyPressed(III)Z"), cancellable = true)
	public void press(long window, int key, int scancode, int action, int mod, CallbackInfo ci) {
		ScreenAccessor accessor = ScreenAccessor.of(GuiUtils.MC.screen);
		if (accessor != null && accessor.getReturnable() != null) {
			if (key == 256) {
				accessor.getReturnable().run();
				ci.cancel();
			}
		}
	}
}
