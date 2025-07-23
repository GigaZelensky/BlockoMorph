package net.blockomorph.screens.config.renderers;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ObjectListRenderer2;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.EnumConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collections;
import java.util.List;

public class EnumConfigRenderer implements ConfigRenderer<EnumConfig<?>> {
	private final EnumListRenderer<Enum<?>> ENUM_LIST_RENDERER = new EnumListRenderer<>(-1, 0xFFFF00FF, 7);
	private final ObjectListRenderer2<EnumConfig<?>, Enum<?>> ENUM_LIST_RENDERER2 = new ObjectListRenderer2<>(-1, 0xFFFF00FF, 7, value -> {
		return value.toString().toLowerCase();
	}, property -> {
		return List.of(property.getEnumClass().getEnumConstants());
	});
	private boolean notClose;

	@Override
	public void renderBackground(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		gui.blit(PLATES_SPRITE, box.getX(), box.getY(), 0, 20, 144, 20, 144, 74);
		ENUM_LIST_RENDERER2.renderName(gui, box.getX() + 97, box.getY() + 7, 42, configInstance.getValue(), -12821534);
	}

	@Override
	public void render(GuiUtils gui, EnumConfig<?> configInstance, Rect2i box) {
		if (ENUM_LIST_RENDERER2.isCurrentProperty(configInstance))
			ENUM_LIST_RENDERER2.render(gui, box.getX() + 97, box.getY() + 7, -3, 10);
	}

	@Override
	public boolean mouseClicked(EnumConfig<?> configInstance, double mouseX, double mouseY, Rect2i box, Screen parentScreen) {
		if (ENUM_LIST_RENDERER2.isCurrentProperty(configInstance) && ENUM_LIST_RENDERER2.mouseClicked(mouseX, mouseY)) {
			return true;
		} else if (GuiUtils.isInBounds(box, mouseX, mouseY)) {//if (GuiUtils.isMouseOver(box.getX() + 94, box.getY() + 4, box.getX() + 139, box.getY() + 17, mouseX, mouseY)) {
			ENUM_LIST_RENDERER2.drop(configInstance, (value) -> {
				GuiUtils.playClickSound();
				MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(configInstance.getName(), value.name()));
			});
			this.notClose = true;
			GuiUtils.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(EnumConfig<?> configInstance, double mouseX, double mouseY, double yOffsetWheel, Rect2i box, Screen parentScreen) {
		if (ENUM_LIST_RENDERER2.isCurrentProperty(configInstance)) {
			return ENUM_LIST_RENDERER2.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}
		return false;
	}

	@Override
	public void init() {
		ENUM_LIST_RENDERER2.close();
	}

	@Override
	public void startClick() {
		this.notClose = false;
	}

	@Override
	public void endClick() {
		if (!this.notClose)
			ENUM_LIST_RENDERER2.close();
	}
}
