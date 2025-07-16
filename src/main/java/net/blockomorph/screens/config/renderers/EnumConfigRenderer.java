package net.blockomorph.screens.config.renderers;

import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.EnumConfig;

public class EnumConfigRenderer implements ConfigRenderer<EnumConfig<?>> {
	@Override
	public void render(GuiUtils gui, EnumConfig<?> configInstance, int plateX, int plateY, int plateLength, int plateHeight) {
		gui.blit(PLATES_SPRITE, plateX, plateY, 0, 20, 144, 20, 144, 74);
	}

	@Override
	public void mouseClicked(EnumConfig<?> configInstance, double mouse, double mouseY) {

	}
}
