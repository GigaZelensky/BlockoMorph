package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class TagEditingOverlay extends AbstractWidget {

	public TagEditingOverlay(String name) {
		super(0, 0, 0, 0, Component.literal(name));
	}

	public void init() {}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
