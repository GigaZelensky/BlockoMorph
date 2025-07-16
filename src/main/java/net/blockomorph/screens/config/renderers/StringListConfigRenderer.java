package net.blockomorph.screens.config.renderers;

import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.ListConfig;
import net.minecraft.client.renderer.Rect2i;

public class StringListConfigRenderer implements ConfigRenderer<ListConfig> {

	@Override
	public void renderBackground(GuiUtils gui, ListConfig configInstance, Rect2i box) {

	}

	@Override
	public void render(GuiUtils gui, ListConfig configInstance, Rect2i box) {

	}

	@Override
	public boolean mouseClicked(ListConfig configInstance, double mouseX, double mouseY, Rect2i box) {
		return false;
	}

	@Override
	public boolean mouseScrolled(ListConfig configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box) {
		return false;
	}
}
