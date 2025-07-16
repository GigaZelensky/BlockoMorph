package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.EnumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class EnumConfigRenderer implements ConfigRenderer<EnumConfig<? extends Enum<?>>> {
	private static final EnumListRenderer ENUM_LIST_RENDERER = new EnumListRenderer(-1, 0xFFFF00FF, 7);

	@Override
	public void render(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 20, 144, 20, 144, 74);
		ENUM_LIST_RENDERER.render(gui, box.getX() + 97, box.getY() + 7, -4, 11, Component.literal(configInstance.getValue().toString()), -1);
	}

	@Override
	public boolean mouseClicked(EnumConfig<?> configInstance, double mouseX, double mouseY, Rect2i box) {
		if (GuiUtils.isMouseOver(box.getX() + 94, box.getY() + 4, box.getX() + 139, box.getY() + 17, mouseX, mouseY)) {
			ENUM_LIST_RENDERER.drop(configInstance.getAllEnumValues(), (value) -> {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
				MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value.name()));
			});
			return true;
		} else if (ENUM_LIST_RENDERER.mouseClicked(mouseX, mouseY)) {
			return true;
		}
		ENUM_LIST_RENDERER.close();
		return false;
	}

	@Override
	public boolean mouseScrolled(EnumConfig<?> configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box) {
		return ENUM_LIST_RENDERER.mouseScrolled(mouseX, mouseY, yOffsetWheel);
	}
}
