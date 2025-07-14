package net.blockomorph.screens.utils;

import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public interface ConfigRenderer<T extends ConfigInstance<?>> {

	void render(GuiUtils gui, T configInstance);

	List<AbstractWidget> getButtons();
}
