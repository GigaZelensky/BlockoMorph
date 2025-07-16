package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class ConfigRenderableList extends ContainerObjectSelectionList<ConfigRenderableList.RenderableConfigInstance<?>> {

	public ConfigRenderableList(Minecraft minecraft, int length, int height, int y, int configPlateHeight) {
		super(minecraft, length, height, y, configPlateHeight);
		for (ConfigInstance<?> option : Config.getInstance().OPTIONS) {
			if (option.canEditedByOperators()) {
				this.addEntry(new RenderableConfigInstance<>(minecraft, option));
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			for (RenderableConfigInstance<?> instance : this.children()) {
				if (instance.mouseClicked(mouseX, mouseY)) {
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, type);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double xScrolled, double yScrolled) {
		for (RenderableConfigInstance<?> instance : this.children()) {
			if (instance.mouseScrolled(x, y, yScrolled)) {
				return true;
			}
		}
		return super.mouseScrolled(x, y, xScrolled, yScrolled);
	}

	public static class RenderableConfigInstance<T extends ConfigInstance<?>> extends ContainerObjectSelectionList.Entry<RenderableConfigInstance<?>> {
		private final Minecraft minecraft;
		private final GuiUtils gui = new GuiUtils();
		private final T instance;
		private final ConfigRenderer<T> renderer;
		private final Rect2i box = new Rect2i(0, 0, 0, 0);

		@SuppressWarnings("unchecked")
		protected RenderableConfigInstance(Minecraft mc, T instance) {
			this.instance = instance;
			this.renderer = (ConfigRenderer<T>)instance.getRenderer();
			this.minecraft = mc;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return this.renderer.getButtons(this.instance);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int numberInList, int y, int x, int length, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
			this.gui.setGuiGraphics(guiGraphics, this.minecraft.font, mouseX, mouseY, delta);
			this.box.setPosition(x, y);
			this.box.setWidth(length);
			this.box.setHeight(height);
			this.renderer.render(this.gui, this.instance, this.box);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.renderer.getButtons(this.instance);
		}

		public boolean mouseClicked(double mouseX, double mouseY) {
			return this.renderer.mouseClicked(this.instance, mouseX, mouseY, this.box);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double yOffsetWheel) {
			return this.renderer.mouseScrolled(this.instance, mouseX, mouseY, yOffsetWheel, this.box);
		}
	}
}
