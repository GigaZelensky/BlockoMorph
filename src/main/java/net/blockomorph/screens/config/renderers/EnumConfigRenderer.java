package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.EnumConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

public class EnumConfigRenderer implements ConfigRenderer<EnumConfig<?>> {
	private final EnumListRenderer ENUM_LIST_RENDERER = new EnumListRenderer(-1, 0xFFFF00FF, 7);
	private boolean notClose;

	@Override
	public void renderBackground(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 20, 144, 20, 144, 74);
		ENUM_LIST_RENDERER.renderName(gui, box.getX() + 97, box.getY() + 7, configInstance.getValue(), -12821534);
	}

	@Override
	public void render(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		if (this.isThisList(configInstance))
			ENUM_LIST_RENDERER.render(gui, box.getX() + 97, box.getY() + 7, -3, 10);
	}

	@Override
	public boolean mouseClicked(EnumConfig<?> configInstance, double mouseX, double mouseY, Rect2i box, Screen parentScreen) {
		if (this.isThisList(configInstance) && ENUM_LIST_RENDERER.mouseClicked(mouseX, mouseY)) {
			return true;
		} else if (this.isInBounds(box, mouseX, mouseY)) {//if (GuiUtils.isMouseOver(box.getX() + 94, box.getY() + 4, box.getX() + 139, box.getY() + 17, mouseX, mouseY)) {
			ENUM_LIST_RENDERER.drop(configInstance.getAllEnumValues(), (value) -> {
				this.playClickSound();
				MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value.name()));
			});
			this.notClose = true;
			this.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(EnumConfig<?> configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, Screen parentScreen) {
		if (this.isThisList(configInstance)) {
			return ENUM_LIST_RENDERER.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}
		return false;
	}

	@Override
	public void init() {
		ENUM_LIST_RENDERER.close();
	}

	@Override
	public void startClick() {
		this.notClose = false;
	}

	@Override
	public void endClick() {
		if (!this.notClose)
			ENUM_LIST_RENDERER.close();
	}

	private boolean isThisList(EnumConfig<?> configInstance) {
		return configInstance.getEnumClass() == ENUM_LIST_RENDERER.getEnumClass();
	}
}
