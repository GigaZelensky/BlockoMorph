package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.EnumConfig;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class EnumConfigRenderer implements ConfigRenderer<EnumConfig<?>> {
	private static final EnumListRenderer ENUM_LIST_RENDERER = new EnumListRenderer(-1, 0xFFFF00FF, 7);

	@Override
	public void renderBackground(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 20, 144, 20, 144, 74);
	}

	@Override
	public void render(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		ENUM_LIST_RENDERER.renderName(gui, box.getX() + 97, box.getY() + 7, Component.literal(configInstance.getValue().toString()), -1);
		if (this.isThisList(configInstance))
			ENUM_LIST_RENDERER.render(gui, box.getX() + 97, box.getY() + 7, -4, 11);
	}

	@Override
	public boolean mouseClicked(EnumConfig<?> configInstance, double mouseX, double mouseY, Rect2i box) {
		if (GuiUtils.isMouseOver(box.getX() + 94, box.getY() + 4, box.getX() + 139, box.getY() + 17, mouseX, mouseY)) {
			ENUM_LIST_RENDERER.drop(configInstance.getAllEnumValues(), (value) -> {
				this.playClickSound();
				MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value.name()));
			});
			return true;
		} else if (this.isThisList(configInstance) && ENUM_LIST_RENDERER.mouseClicked(mouseX, mouseY)) {
			return true;
		}
		ENUM_LIST_RENDERER.close();
		return false;
	}

	@Override
	public boolean mouseScrolled(EnumConfig<?> configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box) {
		if (this.isThisList(configInstance)) {
			return ENUM_LIST_RENDERER.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}
		return false;
	}

	private boolean isThisList(EnumConfig<?> configInstance) {
		return configInstance.getEnumClass() == ENUM_LIST_RENDERER.getEnumClass();
	}
}
