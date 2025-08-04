package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class TagEditingOverlay extends AbstractContainerEventHandler {
	protected int width;
	protected int height;
	protected Consumer<TagEditingOverlay> onChange;
	private final List<AbstractWidget> widgets = new ArrayList<>();

	public TagEditingOverlay() {}

	public void init(int width, int height, Consumer<TagEditingOverlay> onChange) {
		this.widgets.clear();
		this.width = width;
		this.height = height;
		this.onChange = onChange;
	}

	public final void render(GuiUtils gui) {
		this.renderBackground(gui);
		this.widgets.forEach(widget -> {
			widget.render(gui.getGuiGraphics(), gui.getMouseX(), gui.getMouseY(), gui.getTick());
		});
		this.renderInGui(gui);
	}

	protected abstract void renderInGui(GuiUtils gui);
	protected abstract void renderBackground(GuiUtils gui);

	@Override
	public List<? extends GuiEventListener> children() {
		return this.widgets;
	}

	protected void addRenderableWidget(AbstractWidget widget) {
		this.widgets.add(widget);
	}
}
