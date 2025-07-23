package net.blockomorph.screens.morphConfig.widget;

import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ObjectListRenderer;
import net.blockomorph.screens.utils.ScrollerManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockStatePropsRenderer extends AbstractWidget {
	private final ResourceLocation PROPERTIES_SPRITE = GuiUtils.res("textures/screens/properties.png");
	private final PlateRenderer RENDERER = new PlateRenderer(this);
	private final PropertyClickHandler CLICK_HANDLER = new PropertyClickHandler(this);
	private final int SPRITE_HEIGHT = 83;
	private final int SPRITE_LENGTH = 67;
	private final GuiUtils gui = new GuiUtils();
	private final Font font;
	private static final int PLATE_LENGTH = 67;
	private static final int PLATE_HEIGHT = 19;
	private final int maxPlatesCount;
	private final Consumer<BlockState> newStateHandler;
	private final List<Property<?>> renderableProperties;
	private final ScrollerManager<Property<?>> scrollerManager;
	private final Supplier<BlockState> stateSource;
	private BlockState currentState;

	public BlockStatePropsRenderer(Font font, int x, int y, int maxPlatesCount, Supplier<BlockState> stateSource, Consumer<BlockState> newStateHandler) {
		super(x, y, PLATE_LENGTH, PLATE_HEIGHT * maxPlatesCount, CommonComponents.EMPTY);
		this.maxPlatesCount = maxPlatesCount;
		this.font = font;
		this.newStateHandler = newStateHandler;
		this.stateSource = stateSource;
		this.renderableProperties = new ArrayList<>(maxPlatesCount);
		this.scrollerManager = new ScrollerManager<>(null, null, 0, 1, maxPlatesCount, this.renderableProperties);
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, delta);
		BlockState state = this.stateSource.get();
		if (!state.equals(this.currentState)) {
			this.setBlockState(state);
		}
		int y = 0;
		for (Property<?> property : this.renderableProperties) {
			switch (property) {
				case BooleanProperty boolProp -> RENDERER.renderBooleanProperty(boolProp.value(this.currentState), y);
				case IntegerProperty intProp -> RENDERER.renderIntegerProperty(intProp.value(this.currentState), y);
				case EnumProperty<?> enumProp -> RENDERER.renderEnumProperty(enumProp.value(this.currentState), y);
				default -> RENDERER.renderUnknownProperty(property.value(this.currentState), y);
			}
			y = y + PLATE_HEIGHT;
		}
	}

	private void setBlockState(BlockState state) {
		this.currentState = state;
		this.scrollerManager.setScrollOffset(0f);
		this.scrollerManager.setMainList(state.getProperties().stream().toList());
		this.scrollerManager.refreshList();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			if (this.active && this.visible && !this.renderableProperties.isEmpty()) {
				int i = 0;
				for (Property<?> property : this.renderableProperties) {
					int y = this.getY() + i * PLATE_HEIGHT;
					boolean result = switch (property) {
						case BooleanProperty boolProp ->
								CLICK_HANDLER.booleanPropertyClick(boolProp, mouseX, mouseY, y);
						case IntegerProperty intProp -> CLICK_HANDLER.integerPropertyClick(intProp, mouseX, mouseY, y);
						case EnumProperty<?> enumProp -> CLICK_HANDLER.enumPropertyClick(enumProp, mouseX, mouseY, y);
						default -> CLICK_HANDLER.unknownPropertyClick(property, mouseX, mouseY, y);
					};
					if (result) {
						this.releaseRenderers();
						return true;
					}
					i++;
				}
			}
			this.releaseRenderers();
		}
		return false;
	}

	private void releaseRenderers() {
		RENDERER.unknownRenderer.close();
		RENDERER.enumRenderer.close();
	}

	private boolean isThisProperty(ObjectListRenderer<?> renderer, Class<?> valueType) {
		if (renderer.getValuesClass() == null) return false;
		return valueType.isAssignableFrom(renderer.getValuesClass());
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}





	private class PlateRenderer {
		private final BlockStatePropsRenderer widget;
		final EnumListRenderer<Enum<?>> enumRenderer = new EnumListRenderer<>(-1, 0xFFFF00FF, 7);
		final ObjectListRenderer<String> unknownRenderer = new ObjectListRenderer<>(-1, -256, 7, s -> s);
		private PlateRenderer(BlockStatePropsRenderer renderer) {
			this.widget = renderer;
		}

		private void renderPlate(int yOffset, int number) {
			this.widget.gui.blit(PROPERTIES_SPRITE, this.widget.getX(), this.widget.getY() + yOffset, 0, PLATE_HEIGHT * number, SPRITE_LENGTH, PLATE_HEIGHT, SPRITE_LENGTH, SPRITE_HEIGHT);
		}

		private void renderBooleanProperty(Property.Value<Boolean> value, int yOffset) {
			this.renderPlate(yOffset, 2);
			if (value.value()) {
				this.widget.gui.blit(PROPERTIES_SPRITE, this.widget.getX() + 44, this.widget.getY() + yOffset + 6, 0, 76, 15, 7, SPRITE_LENGTH, SPRITE_HEIGHT);
			}
		}

		private void renderIntegerProperty(Property.Value<Integer> value, int yOffset) {
			this.renderPlate(yOffset, 1);
			this.widget.gui.drawString(Component.literal(value.value().toString()), this.widget.getX() + 41, this.widget.getY() + yOffset + 7, -1, false);
		}

		private <T extends Enum<T> & StringRepresentable> void renderEnumProperty(Property.Value<T> value, int yOffset) {
			this.renderPlate(yOffset, 0);
			ScreenPosition pos = this.getListPropPos(yOffset);
			this.enumRenderer.renderName(this.widget.gui, pos.x(), pos.y(), 20, value.value(), -12821534);
			if (this.widget.isThisProperty(this.enumRenderer, value.property().getValueClass())) {
				this.enumRenderer.render(this.widget.gui, pos.x(), pos.y(), -1, 8);
			}
		}

		private <T extends Comparable<T>> void renderUnknownProperty(Property.Value<T> value, int yOffset) {
			this.renderPlate(yOffset, 3);
			ScreenPosition pos = this.getListPropPos(yOffset);
			this.unknownRenderer.renderName(this.widget.gui, pos.x(), pos.y(), 20, value.property().getName(value.value()), -1);
			if (this.widget.isThisProperty(this.unknownRenderer, value.property().getValueClass())) {
				this.unknownRenderer.render(this.widget.gui, pos.x(), pos.y(), -1, 8);
			}
		}

		private ScreenPosition getListPropPos(int yOffset) {
			int textX = this.widget.getX() + 41;
			int textY = this.widget.getY() + yOffset + 7;
			return new ScreenPosition(textX, textY);
		}
	}



	private class PropertyClickHandler {
		private final BlockStatePropsRenderer widget;

		private PropertyClickHandler(BlockStatePropsRenderer widget) {
			this.widget = widget;
		}

		private boolean booleanPropertyClick(BooleanProperty property, double mouseX, double mouseY, int yOffset) {
			if (mouseY >= yOffset && mouseY <= yOffset + PLATE_HEIGHT) {
				Optional<Boolean> value = this.widget.currentState.getOptionalValue(property);
				if (value.isPresent()) {
					BlockState state = this.widget.currentState.trySetValue(property, !value.get());
					this.widget.newStateHandler.accept(state);
					this.widget.playDownSound(GuiUtils.MC.getSoundManager());
					return true;
				}
			}
			return false;
		}

		private boolean integerPropertyClick(IntegerProperty property, double mouseX, double mouseY, int yOffset) {
			return false;
		}

		private <T extends Enum<T> & StringRepresentable> boolean enumPropertyClick(EnumProperty<T> property, double mouseX, double mouseY, int yOffset) {
			if (mouseY >= yOffset && mouseY <= yOffset + PLATE_HEIGHT) {

			}
			return false;
		}

		private <T extends Comparable<T>> boolean unknownPropertyClick(Property<T> property, double mouseX, double mouseY, int yOffset) {

		}
	}
}
