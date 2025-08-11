package net.blockomorph.screens.config.renderers;

import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.config.ListOptionEditingMorphScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.BlockListConfig;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

public class BlockListConfigRenderer implements ConfigRenderer<BlockListConfig> {
	private static final ResourceLocation BUTTON_SPRITE = GuiUtils.res("textures/screens/list_but.png");
	private final int BUTTON_SIZE = 16;

	@Override
	public void renderBackground(GuiUtils gui, BlockListConfig configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 40, 144, 20, 144, 74);
		int buttonX = box.getX() + 125;
		int buttonY = box.getY() + 2;
		boolean hovered = this.isButtonHovered(buttonX, buttonY, gui.getMouseX(), gui.getMouseY());
		gui.blit(BUTTON_SPRITE, buttonX, buttonY, 0, hovered ? BUTTON_SIZE : 0, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE * 2);
	}

	@Override
	public void render(GuiUtils gui, BlockListConfig configInstance, Rect2i box) {}

	@Override
	public boolean mouseClicked(BlockListConfig configInstance, double mouseX, double mouseY, Rect2i box, ConfigRenderingContext context) {
		int buttonX = box.getX() + 125;
		int buttonY = box.getY() + 2;
		if (this.isButtonHovered(buttonX, buttonY, mouseX, mouseY)) {
			GuiUtils.playClickSound();
			GuiUtils.MC.setScreen(new ListOptionEditingMorphScreen(configInstance, context));
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(BlockListConfig configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, ConfigRenderingContext context) {
		return false;
	}

	@Override
	public int getOptionColor(BlockListConfig configInstance) {
		return -6710887;
	}

	private boolean isButtonHovered(int buttonX, int buttonY, double mouseX, double mouseY) {
		return GuiUtils.isMouseOver(buttonX, buttonY, buttonX + BUTTON_SIZE, buttonY + BUTTON_SIZE, mouseX, mouseY);
	}
}
