package net.blockomorph.screens.utils;

import net.blockomorph.BlockomorphServer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiUtils {
	private GuiGraphics GUI;
	private Font font;

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(BlockomorphServer.MOD_ID, path);
	}

	public static ResourceLocation vanillaRes(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}

	public void setGuiGraphics(GuiGraphics gui, Font font) {
		GUI = gui;
		this.font = font;
	}

	public GuiGraphics getGuiGraphics() {
		return GUI;
	}

	/* HINT:
			X - up left corner
			Y - up left corner
			u - start of texture X (left up corner)
			y - start of texture Y (left up corner)
			uvMaxX - length of start of UV
			uvMaxY - length of start of UV
			max X - length
			max Y - height
		 */
	public void blit(ResourceLocation resourceLocation, int x, int y, float u, float v, int uvMaxX, int uvMaxY, int maxX, int maxY) {
		GUI.blit(RenderType::guiTextured, resourceLocation, x, y, u, v, uvMaxX, uvMaxY, maxX, maxY);
	}

	public void blitMonoImage(ResourceLocation resourceLocation, int x, int y, int maxSizeX, int maxSizeY) {
		this.blit(resourceLocation, x, y, 0, 0, maxSizeX, maxSizeY, maxSizeX, maxSizeY);
	}

	public void renderTooltip(Component text, int mouseX, int mouseY) {
		GUI.renderTooltip(this.font, text, mouseX, mouseY);
	}
}
