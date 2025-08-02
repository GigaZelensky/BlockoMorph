package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.blockomorph.screens.morphConfig.propertiesWidget.BlockStatePropsRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class BlockStateSelectorOverlay extends TagEditingOverlay {
	private static final int imageLength = 159;
	private static final int imageHeight = 131;
	private final Consumer<BlockState> handler;
	private BlockState state;
	private int leftPos;
	private int topPos;
	private static final ResourceLocation MENU = GuiUtils.res("textures/screens/state_selector.png");
	private BlockStatePropsRenderer propsRenderer;
	private Button exit;
	private Button typeEdit;

	public BlockStateSelectorOverlay(BlockState state, Consumer<BlockState> handler) {
		super("BlockState editing overlay");
		this.handler = handler;
		this.state = state;
	}

	@Override
	public void renderInGui(GuiUtils gui) {
		gui.blitMonoImage(MENU, this.leftPos, this.topPos, imageLength, imageHeight);
		this.renderString(gui);
		gui.renderBlockInGui(this.state, null, this.leftPos + 38.5f, this.topPos + 76.5f, 36f);
		gui.renderAdditionalOnBlock(this.state, this.leftPos + 16.5f, this.topPos + 40.5f, 55f);
		this.propsRenderer.render(gui);
		this.exit.render(gui.getGuiGraphics(), gui.getMouseX(), gui.getMouseY(), gui.getTick());
		if (GuiUtils.isMouseOver(this.leftPos + 8, this.topPos + 18, this.leftPos + 69, this.topPos + 79, gui.getMouseX(), gui.getMouseY())) {
			gui.renderTooltip(this.state.getBlock().getName(), gui.getMouseX(), gui.getMouseY());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			if (this.exit.mouseClicked(mouseX, mouseY, type)) {
				return true;
			}
			return this.propsRenderer.mouseClicked(mouseX, mouseY, type);
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		return this.propsRenderer.mouseScrolled(mouseX, mouseY, yWheelOffset);
	}

	private void renderString(GuiUtils gui) {
		String blockName = this.state.getBlock().getName().getString();
		if (gui.getFont().width(blockName) > 68) {
			blockName = gui.getFont().plainSubstrByWidth(blockName, 62) + "...";
		}
		gui.drawString(Component.literal(blockName), this.leftPos + 6, this.topPos + 6, 4210752, false);
		gui.drawString(Component.literal("BlockState"), this.leftPos + 83, this.topPos + 11, 4210752, false);
	}

	@Override
	public void init(int width, int height, Consumer<TagEditingOverlay> onChange) {
		super.init(width, height, onChange);
		this.leftPos = (this.width - imageLength) / 2;
		this.topPos = (this.height - imageHeight) / 2;
		this.propsRenderer = new BlockStatePropsRenderer(this.leftPos + 79, this.topPos + 20, 5, () -> this.state, newState -> {
			this.state = newState;
			this.handler.accept(newState);
		});
		this.exit = Button.builder(CommonComponents.GUI_DONE, b -> {
			onChange.accept(null);
		}).bounds(this.leftPos + 8, this.topPos + 105, 61, 20).build();
	}
}
