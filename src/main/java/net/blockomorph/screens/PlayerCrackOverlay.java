
package net.blockomorph.screens;

import com.mojang.blaze3d.platform.Window;
import net.blockomorph.utils.PlayerAccessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.RenderType;

public class PlayerCrackOverlay {
	public static void render(GuiGraphics guiGraphics, DeltaTracker tickDelta) {
		Window window = Minecraft.getInstance().getWindow();
		int w = window.getGuiScaledWidth();
		int h = window.getGuiScaledHeight();
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {

		    RenderSystem.setShaderColor(1, 1, 1, 1);

		    int k = ((PlayerAccessor)entity).getBiggestProgress();
		    
			if (k >= 0 && k < 10) guiGraphics.blit(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + k + ".png"), 0, 0, 16, 16, w, h, w, h);

		    RenderSystem.setShaderColor(1, 1, 1, 1);
		}
	}
}
