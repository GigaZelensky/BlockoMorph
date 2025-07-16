package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface ConfigRenderer<T extends ConfigInstance<?>> {
	ResourceLocation PLATES_SPRITE = GuiUtils.res("textures/screens/configs.png");

	void render(GuiUtils gui, T configInstance, Rect2i box);

	boolean mouseClicked(T configInstance, double mouseX, double mouseY, Rect2i box);

	boolean mouseScrolled(T configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box);

	default List<AbstractWidget> getButtons(T configInstance) {
		return List.of();
	}

	default boolean isInBounds(Rect2i box, double mouseX, double mouseY) {
		return GuiUtils.isMouseOver(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), mouseX, mouseY);
	}
}
