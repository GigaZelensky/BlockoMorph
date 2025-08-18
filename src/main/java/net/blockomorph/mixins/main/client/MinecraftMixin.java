package net.blockomorph.mixins.main.client;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.accessors.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Inject(method = "setScreen", at = @At("TAIL"))
	public void set(Screen newScreen, CallbackInfo ci) {
		if (newScreen != null && GuiUtils.getRejectingAction() != null) {
			ScreenAccessor.of(newScreen).setReturnable(GuiUtils.getRejectingAction());
			GuiUtils.setForceRejectButtonBeforeOpeningScreen(null);
		}
	}
}
