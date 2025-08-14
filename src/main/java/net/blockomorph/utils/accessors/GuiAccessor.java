package net.blockomorph.utils.accessors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;

public interface GuiAccessor {

	GuiRenderState extractState();

	static GuiRenderState extractStateFrom(GuiGraphics gui) {
		return ((GuiAccessor)gui).extractState();
	}
}
