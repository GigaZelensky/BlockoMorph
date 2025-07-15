package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.BooleanConfig;

public class BooleanConfigRenderer implements ConfigRenderer<BooleanConfig> {
	@Override
	public void render(GuiUtils gui, BooleanConfig configInstance, int plateX, int plateY, int plateLength, int plateHeight) {
		gui.blit(PLATES_SPRITE, plateX, plateY, 0, 0, 144, 20, 144, 74);
		if (configInstance.getValue()) {
			gui.blit(PLATES_SPRITE, plateX + 105, plateY + 3, 0, 60, 24, 14, 144, 74);
		}
	}

	@Override
	public void mouseClicked(BooleanConfig configInstance, double mouse, double mouseY) {
		boolean value = !configInstance.getValue();
		MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value + ""));
	}
}
