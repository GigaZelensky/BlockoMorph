package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.BooleanConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

public class BooleanConfigRenderer implements ConfigRenderer<BooleanConfig> {
	@Override
	public void renderBackground(GuiUtils gui, BooleanConfig configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 0, 144, 20, 144, 74);
		if (configInstance.getValue()) {
			gui.blit(PLATES_SPRITE, box.getX() + 105, box.getY() + 3, 0, 60, 24, 14, 144, 74);
		}
	}

	@Override
	public void render(GuiUtils gui, BooleanConfig configInstance, Rect2i box) {}

	@Override
	public boolean mouseClicked(BooleanConfig configInstance, double mouseX, double mouseY, Rect2i box, Screen parentScreen) {
		if (GuiUtils.isInBounds(box, mouseX, mouseY)) {
			boolean value = !configInstance.getValue();
			MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value + ""));
			GuiUtils.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(BooleanConfig configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, Screen parentScreen) {
		return false;
	}
}
