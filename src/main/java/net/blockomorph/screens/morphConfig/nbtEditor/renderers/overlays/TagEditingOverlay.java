package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class TagEditingOverlay extends AbstractWidget {
	protected int width;
	protected int height;
	protected Consumer<TagEditingOverlay> onChange;

	public TagEditingOverlay(String name) {
		super(0, 0, 0, 0, Component.literal(name));
	}

	public void init(int width, int height, Consumer<TagEditingOverlay> onChange) {
		this.width = width;
		this.height = height;
		this.onChange = onChange;
	}

	@Override
	protected final void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {}

	public abstract void renderInGui(GuiUtils gui);

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
