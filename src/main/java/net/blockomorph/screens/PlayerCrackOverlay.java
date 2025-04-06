
package net.blockomorph.screens;

import net.blockomorph.utils.PlayerAccessor;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.client.renderer.RenderType;

@EventBusSubscriber({Dist.CLIENT})
public class PlayerCrackOverlay {
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		int w = event.getGuiGraphics().guiWidth();
		int h = event.getGuiGraphics().guiHeight();
		Player entity = Minecraft.getInstance().player;
		if (entity != null) {

		    int k = ((PlayerAccessor)entity).getBiggestProgress();

		    RenderSystem.setShaderColor(1, 1, 1, 1);
		    
			if (k >= 0 && k < 10) event.getGuiGraphics().blit(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("textures/block/destroy_stage_" + k + ".png"), 0, 0, 16, 16, w, h, w, h);

		    RenderSystem.setShaderColor(1, 1, 1, 1);
			
		}
	}
}
