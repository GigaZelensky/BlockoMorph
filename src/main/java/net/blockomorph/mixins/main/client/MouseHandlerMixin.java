package net.blockomorph.mixins.main.client;

import com.mojang.blaze3d.platform.Window;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.accessors.ScreenAccessor;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Shadow public abstract double getScaledXPos(Window p_412944_);

	@Shadow public abstract double getScaledYPos(Window p_412942_);

	@Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;mouseClicked(DDI)Z"), cancellable = true)
	public void press(long window, int button, int action, int mods, CallbackInfo ci) {
		if (GuiUtils.MC.screen != null) {
			ScreenAccessor accessor = ScreenAccessor.of(GuiUtils.MC.screen);
			Window window2 = GuiUtils.MC.getWindow();
			if (accessor.getReturnable() != null && accessor.getReturnButton().mouseClicked(this.getScaledXPos(window2), this.getScaledYPos(window2), button)) {
				ci.cancel();
			}
		}
	}
}
