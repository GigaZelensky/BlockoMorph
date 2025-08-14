package net.blockomorph.mixins.main.client.graphic;

import net.blockomorph.utils.accessors.GuiAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements GuiAccessor {
	@Shadow @Final private GuiRenderState guiRenderState;

	@Override
	public GuiRenderState extractState() {
		return this.guiRenderState;
	}
}
