package net.blockomorph.mixins.temp;

import net.blockomorph.screens.GuiBlockRenderState;
import net.blockomorph.utils.accessors.temp.GuiStateAccessor;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.ScreenArea;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Debug(export = true)
@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin implements GuiStateAccessor {
	List<GuiBlockRenderState> states = new ArrayList<>();

	@Shadow public abstract void submitPicturesInPictureState(PictureInPictureRenderState pictureInPictureRenderState);

	public void loadList(List<GuiBlockRenderState> states) {
		this.states = states;
	}

	public List<GuiBlockRenderState> get() {
		return this.states;
	}
}
