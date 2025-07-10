package net.blockomorph.utils.accessors.temp;

import net.blockomorph.screens.GuiBlockRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;

import java.util.List;

public interface GuiStateAccessor {

	void loadList(List<GuiBlockRenderState> states);

	List<GuiBlockRenderState> get();

	static void load(GuiRenderState rend, List<GuiBlockRenderState> states) {
		((GuiStateAccessor) rend).loadList(states);
	}
}
