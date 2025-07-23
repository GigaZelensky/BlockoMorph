package net.blockomorph.screens.morphConfig.widget;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ObjectListRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.Consumer;

public class EnumPropertyRenderer<T extends Enum<T> & StringRepresentable> implements PropertyRenderer<T, EnumProperty<T>> {
	private final ObjectListRenderer<EnumProperty<T>, T> RENDERER = new ObjectListRenderer<>(-1, 0xFFFF00FF, 7, EnumProperty::getName, EnumProperty::getPossibleValues);
	private boolean notClose;

	@Override
	public void renderBackground(GuiUtils gui, EnumProperty<T> property, T value, Rect2i box) {
		this.renderPlate(gui, box, 0);
		RENDERER.renderName(gui, box.getX() + 41, box.getY() + 6, 20, property, value, -12821534);
	}

	@Override
	public void render(GuiUtils gui, EnumProperty<T> property, T value, Rect2i box) {
		if (RENDERER.isCurrentProperty(property)) {
			RENDERER.render(gui, box.getX() + 41, box.getY() + 6, -1, 8);
		}
	}

	@Override
	public boolean mouseClicked(Consumer<BlockState> newStateHandler, BlockState state, EnumProperty<T> property, T value, double mouseX, double mouseY, Rect2i box) {
		if (RENDERER.isCurrentProperty(property) && RENDERER.mouseClicked(mouseX, mouseY)) {
			return true;
		} else if (GuiUtils.isInBounds(box, mouseX, mouseY)) {
			RENDERER.drop(property, newValue -> {
				GuiUtils.playClickSound();
				BlockState newState = state.trySetValue(property, newValue);
				newStateHandler.accept(newState);
			});
			GuiUtils.playClickSound();
			this.notClose = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(Consumer<BlockState> newStateHandler, BlockState state, EnumProperty<T> property, T value, double mouseX, double mouseY, double yOffsetWheel, Rect2i box) {
		if (RENDERER.isCurrentProperty(property)) {
			return RENDERER.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}
		return false;
	}

	@Override
	public void startClick() {
		this.notClose = false;
	}

	@Override
	public void endClick() {
		if (!this.notClose) {
			RENDERER.close();
		}
	}
}
