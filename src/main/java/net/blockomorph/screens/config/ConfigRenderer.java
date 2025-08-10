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

import java.util.ArrayList;
import java.util.List;

public interface ConfigRenderer<T extends ConfigInstance<?>> {
	String LOCAL_KEY = "blockomorph.config_option.";
	int MAX_NAME_WIDTH = 85;
	ResourceLocation PLATES_SPRITE = GuiUtils.res("textures/screens/configs.png");

	void renderBackground(GuiUtils gui, T configInstance, Rect2i box);

	void render(GuiUtils gui, T configInstance, Rect2i box);

	boolean mouseClicked(T configInstance, double mouseX, double mouseY, Rect2i box, Screen parentScreen);

	boolean mouseScrolled(T configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, Screen parentScreen);

	default void renderOptionName(GuiUtils gui, T configInstance, Rect2i box) {
		MutableComponent name = Component.translatable(LOCAL_KEY + configInstance.getName());
		int stringLength = gui.getFont().width(name.getString());
		if (stringLength > MAX_NAME_WIDTH) {
			name = Component.literal(gui.getFont().plainSubstrByWidth(name.getString(), MAX_NAME_WIDTH) + "..");
		}
		gui.drawString(name, box.getX() + 4, box.getY() + 5, this.getOptionColor(configInstance), false);
	}

	default void renderTooltip(GuiUtils gui, T configInstance, Rect2i box) {
		if (GuiUtils.isMouseOver(box.getX(), box.getY(), box.getX() + MAX_NAME_WIDTH + 5, box.getY() + box.getHeight(), gui.getMouseX(), gui.getMouseY())) {
			List<Component> tooltips = new ArrayList<>();
			Component tooltip = this.getTooltip(gui, configInstance);
			if (tooltip != null) {
				tooltips.add(tooltip);
			}
			if (configInstance.getTooltip() != null) {
				tooltips.add(configInstance.getTooltip());
			}
			gui.renderTooltip(tooltips, gui.getMouseX(), gui.getMouseY());
		}
	}

	@Nullable
	default Component getTooltip(GuiUtils gui, T configInstance) {
		MutableComponent name = Component.translatable(LOCAL_KEY + configInstance.getName());
		if (gui.getFont().width(name.getString()) > MAX_NAME_WIDTH) {
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
