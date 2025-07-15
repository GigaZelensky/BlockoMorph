package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.HashMap;
import java.util.List;

public class ConfigRenderableList extends ContainerObjectSelectionList<ConfigRenderableList.RenderableConfigInstance> {

	public ConfigRenderableList(Minecraft minecraft, int length, int height, int y, int configPlateHeight) {
		super(minecraft, length, height, y, configPlateHeight);
		for (ConfigInstance<?> option : Config.getInstance().OPTIONS) {
			if (option.canEditedByOperators()) {
				this.addEntry(new RenderableConfigInstance(minecraft, option));
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			RenderableConfigInstance entry = this.getEntryAtPosition(mouseX, mouseY);
			if (entry != null) {
				entry.instance.getRenderer().mouseClicked(entry.instance, mouseX, mouseY);
			}
		}
		return super.mouseClicked(mouseX, mouseY, type);
	}

	public static class RenderableConfigInstance extends ContainerObjectSelectionList.Entry<RenderableConfigInstance> {
		private final Minecraft minecraft;
		private final GuiUtils gui = new GuiUtils();
		private final ConfigInstance<?> instance;

		protected RenderableConfigInstance(Minecraft mc, ConfigInstance<?> instance) {
			this.instance = instance;
			this.minecraft = mc;
		}
		@Override
		public List<? extends NarratableEntry> narratables() {
			return this.instance.getRenderer().getButtons(this.instance);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int numberInList, int y, int x, int length, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
			gui.setGuiGraphics(guiGraphics, this.minecraft.font, mouseX, mouseY, delta);
			this.instance.getRenderer().render(this.gui, this.instance, x, y, length,height);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.instance.getRenderer().getButtons(this.instance);
		}
	}
}
