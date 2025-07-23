package net.blockomorph.screens.morphConfig.widget;

import net.blockomorph.screens.config.ConfigRenderableList;
import net.blockomorph.screens.config.ConfigRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ScrollerManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockStatePropsRenderer2 {
	private final HashMap<Class<?>, PropertyRenderer<?, ?>> RENDERERS = new HashMap<>();
	private final int x, y;
	private static final int PLATE_LENGTH = 67;
	private static final int PLATE_HEIGHT = 19;
	private final Consumer<BlockState> newStateHandler;
	private final List<RenderableProperty<?, ?>> renderableProperties;
	private final ScrollerManager<RenderableProperty<?, ?>> scrollerManager;
	private final Supplier<BlockState> stateSource;
	private BlockState currentState;

	public BlockStatePropsRenderer2(int x, int y, int maxPlatesCount, Supplier<BlockState> stateSource, Consumer<BlockState> newStateHandler) {
		this.x = x;
		this.y = y;
		this.stateSource = stateSource;
		this.newStateHandler = newStateHandler;
		this.renderableProperties = new ArrayList<>(maxPlatesCount);
		this.scrollerManager = new ScrollerManager<>(null, null, 0, 1, maxPlatesCount, this.renderableProperties);
	}

	public void render(GuiUtils gui) {
		BlockState state = this.stateSource.get();
		if (!state.equals(this.currentState)) {
			this.setBlockState(state);
		}
		this.renderProperties(gui, false);
		this.renderProperties(gui, true);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			RENDERERS.forEach((clazz, render) -> render.startClick());
			for (RenderableProperty<?, ?> instance : this.renderableProperties) {
				if (instance.mouseClicked(mouseX, mouseY)) {
					RENDERERS.forEach((clazz, render) -> render.endClick());
					return true;
				}
			}
			RENDERERS.forEach((clazz, render) -> render.endClick());
		}
		return false;
	}

	public boolean mouseScrolled(double x, double y, double yScrolled) {
		for (RenderableProperty<?, ?> instance : this.renderableProperties) {
			if (instance.mouseScrolled(x, y, yScrolled)) {
				return true;
			}
		}
		return this.scrollerManager.mouseScrolled(yScrolled);
	}

	private void renderProperties(GuiUtils gui, boolean mainPhase) {
		for (int i = 0; i < this.renderableProperties.size(); i++) {
			int yStart = i * PLATE_HEIGHT;
			this.renderableProperties.get(i).render(gui, mainPhase, yStart);
		}
	}

	private void setBlockState(BlockState state) {
		this.currentState = state;
		this.scrollerManager.setScrollOffset(0f);
		List<RenderableProperty<?, ?>> properties = new ArrayList<>();
		for (Property<?> property : this.currentState.getProperties()) {
			properties.add(new RenderableProperty<>(property));
		}
		this.scrollerManager.setMainList(properties);
		this.scrollerManager.refreshList();
	}

	private class RenderableProperty<VALUE extends Comparable<VALUE>, T extends Property<VALUE>> {
		private final Rect2i box = new Rect2i(0, 0, 0, 0);
		private final PropertyRenderer<VALUE, T> renderer;
		private final T property;
		private final VALUE value;

		@SuppressWarnings("unchecked")
		private RenderableProperty(T property) {
			this.property = property;
			this.value = BlockStatePropsRenderer2.this.currentState.getValue(property);
			PropertyRenderer<VALUE, T> renderer = (PropertyRenderer<VALUE, T>)RENDERERS.get(property.getClass());
			if (renderer != null) {
				this.renderer = renderer;
			} else {
				this.renderer = null;//TODO
			}
		}

		public void render(GuiUtils gui, boolean mainPhase, int yOffset) {
			this.box.setPosition(BlockStatePropsRenderer2.this.x, BlockStatePropsRenderer2.this.y + yOffset);
			this.box.setWidth(PLATE_LENGTH);
			this.box.setHeight(PLATE_HEIGHT);
			if (mainPhase) {
				this.renderer.render(gui, this.property, this.value, this.box);
			} else {
				this.renderer.renderBackground(gui, this.property, this.value, this.box);
				//this.renderer.renderOptionName(gui, this.instance, this.box);
			}
		}

		public boolean mouseClicked(double mouseX, double mouseY) {
			return this.renderer.mouseClicked(BlockStatePropsRenderer2.this.newStateHandler, BlockStatePropsRenderer2.this.currentState, this.property, this.value, mouseX, mouseY, this.box);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double yOffsetWheel) {
			return this.renderer.mouseScrolled(BlockStatePropsRenderer2.this.newStateHandler, BlockStatePropsRenderer2.this.currentState, this.property, this.value, mouseX, mouseY, yOffsetWheel, this.box);
		}
	}
}
