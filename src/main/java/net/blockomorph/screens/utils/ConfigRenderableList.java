package net.blockomorph.screens.utils;

import net.blockomorph.utils.config.ConfigInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.HashMap;
import java.util.List;

public class ConfigRenderableList extends ContainerObjectSelectionList<ConfigRenderableList.RenderableConfigInstance> {
	protected <T extends ConfigInstance<?>> final HashMap<Class<T>, ConfigRenderer<T>> RENDERERS = new HashMap<>();
	public ConfigRenderableList(Minecraft minecraft, int length, int height, int y, int configPlateHeight) {
		super(minecraft, length, height, y, configPlateHeight);
	}



	public static class RenderableConfigInstance extends ContainerObjectSelectionList.Entry<RenderableConfigInstance> {
		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of();
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of();
		}
	}
}
