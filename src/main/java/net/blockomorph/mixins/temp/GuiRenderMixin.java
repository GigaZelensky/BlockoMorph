package net.blockomorph.mixins.temp;

import com.llamalad7.mixinextras.sugar.Local;
import net.blockomorph.screens.GuiBlockRenderState;
import net.blockomorph.utils.accessors.temp.GuiStateAccessor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRenderMixin {

	@Shadow @Final
	GuiRenderState renderState;

	@Shadow protected abstract <T extends PictureInPictureRenderState> void preparePictureInPictureState(T pictureInPictureRenderState, int i);

	//@Inject(method = "preparePictureInPicture", at = @At("TAIL"))
	/*public void run(CallbackInfo ci, @Local int i) {
		if (this.renderState instanceof GuiStateAccessor acc) {
			acc.get().forEach((GuiBlockRenderState state) -> {
				this.preparePictureInPictureState(state, i);
			});
		}
	}*/
}
