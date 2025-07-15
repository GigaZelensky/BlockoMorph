package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface ConfigRenderer<T extends ConfigInstance<?>> {
	ResourceLocation PLATES_SPRITE = GuiUtils.res("textures/screens/configs.png");

	void render(GuiUtils gui, T configInstance, int plateX, int plateY, int plateLength, int plateHeight);

	void mouseClicked(T configInstance, double mouse, double mouseY);

	default List<AbstractWidget> getButtons(T configInstance) {
		return List.of();
	}
}
