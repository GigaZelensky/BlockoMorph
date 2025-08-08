package net.blockomorph.mixins.main.client.graphic;

import net.blockomorph.utils.MorphUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
@Debug(export = true)
public class GuiMixin {

	@Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
	private void renderHearts(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
		if (MorphUtils.onHudRender(guiGraphics)) ci.cancel();
	}

	@Inject(method = "*()V", at = @At("HEAD"), cancellable = true)
	private void run(GuiGraphics gui, CallbackInfo ci) {

	}
}
