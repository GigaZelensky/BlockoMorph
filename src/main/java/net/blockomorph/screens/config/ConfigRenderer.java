package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

public interface ConfigRenderer<T extends ConfigInstance<?>> {
	ResourceLocation PLATES_SPRITE = GuiUtils.res("textures/screens/configs.png");

	void renderBackground(GuiUtils gui, T configInstance, Rect2i box);

	void render(GuiUtils gui, T configInstance, Rect2i box);

	boolean mouseClicked(T configInstance, double mouseX, double mouseY, Rect2i box, Screen parentScreen);

	boolean mouseScrolled(T configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, Screen parentScreen);

	default boolean isInBounds(Rect2i box, double mouseX, double mouseY) {
		return GuiUtils.isMouseOver(box.getX(), box.getY(), box.getX() + box.getWidth(), box.getY() + box.getHeight(), mouseX, mouseY);
	}

	default void playClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
	}

	default void renderOptionName(GuiUtils gui, T configInstance, Rect2i box) {
		MutableComponent name = Component.translatable("blockomorph.config_option." + configInstance.getName());
		int stringLength = gui.getFont().width(name.getString());
		if (stringLength > 85) {
			name = Component.literal(gui.getFont().plainSubstrByWidth(name.getString(), 85) + "..");
		}
		gui.drawString(name, box.getX() + 4, box.getY() + 5, this.getOptionColor(configInstance), false);
	}

	default void renderTooltip(GuiUtils gui, T configInstance, Rect2i box) {
		if (GuiUtils.isMouseOver(box.getX(), box.getY(), box.getX() + 90, box.getY() + box.getHeight(), gui.getMouseX(), gui.getMouseY())) {
			Component tooltip = this.getTooltip(gui, configInstance);
			if (tooltip != null) {
				gui.renderTooltip(this.getTooltip(gui, configInstance), gui.getMouseX(), gui.getMouseY());
			}
		}
	}

	@Nullable
	default Component getTooltip(GuiUtils gui, T configInstance) {
		MutableComponent name = Component.translatable("blockomorph.config_option." + configInstance.getName());
		if (gui.getFont().width(name.getString()) > 85) {
			return name;
		}
		return null;
	}

	default int getOptionColor(T configInstance) {
		return -1;
	}

	default void startClick() {}

	default void endClick() {}

	default void init() {}
}
