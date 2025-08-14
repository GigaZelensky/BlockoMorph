package net.blockomorph.mixins.main.client.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class GuiFireRenderMixin {

	@Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
	private static void render(PoseStack p_110730_, MultiBufferSource p_382958_, CallbackInfo ci) {
		if (GuiUtils.MC.player != null && PlayerAccessor.of(GuiUtils.MC.player).isActive()) {
			ci.cancel();
		}
	}
}