package net.blockomorph.screens.overlay;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class BlockHeartOverlay implements Overlay {
	private static final ResourceLocation BAR_IMAGE = GuiUtils.res("textures/screens/icons.png");

	@Override
	public void render(GuiUtils gui, int screenWidth, int screenHeight) {
		if (GuiUtils.MC.player instanceof PlayerAccessor player && player.isActive() && GuiUtils.MC.gameMode != null && GuiUtils.MC.gameMode.canHurtPlayer()) {
			int progress = player.getBiggestProgress();
			int x = screenWidth/2 - 90;
			int y = screenHeight - 38;
			gui.blit(BAR_IMAGE, x - 1, y - 1, 0, progress == 9 ? 9 : 0, 81, 9, 81, 18);
			TextureAtlasSprite sprite = GuiUtils.blockRenderer.getBlockModel(player.getBlockState(InPlayerBlockPos.ZERO)).particleIcon();
			for (int i = 0; i < 10; i++) {
				if (i < 9 - progress) {
					gui.renderFromSpriteClass(sprite, x + i*8, y, 7, 7);
				}
			}
		}
	}
}
