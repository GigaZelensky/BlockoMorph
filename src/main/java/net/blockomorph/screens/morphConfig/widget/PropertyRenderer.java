package net.blockomorph.screens.morphConfig.widget;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Consumer;

import static net.blockomorph.screens.morphConfig.widget.BlockStatePropsRenderer.PLATE_HEIGHT;

public interface PropertyRenderer<VALUE extends Comparable<VALUE>, T extends Property<VALUE>> {
	ResourceLocation PROPERTIES_SPRITE = GuiUtils.res("textures/screens/properties.png");
	int SPRITE_HEIGHT = 83;
	int SPRITE_LENGTH = 67;

	void renderBackground(GuiUtils gui, T property, VALUE value, Rect2i box);

	void render(GuiUtils gui, T property, VALUE value, Rect2i box);

	boolean mouseClicked(Consumer<BlockState> newStateHandler, BlockState state, T property, VALUE value, double mouseX, double mouseY, Rect2i box);

	boolean mouseScrolled(Consumer<BlockState> newStateHandler, BlockState state, T property, VALUE value, double mouseX, double mouseY, double yOffsetWheel, Rect2i box);

	default void renderPlate(GuiUtils gui, Rect2i box, int number) {
		gui.blit(PROPERTIES_SPRITE, box.getX(), box.getY(), 0, PLATE_HEIGHT * number, SPRITE_LENGTH, PLATE_HEIGHT, SPRITE_LENGTH, SPRITE_HEIGHT);
	}

	default void startClick() {}

	default void endClick() {}
}
