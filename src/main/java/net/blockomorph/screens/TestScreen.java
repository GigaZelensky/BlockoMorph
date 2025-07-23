package net.blockomorph.screens;

import net.blockomorph.screens.morphConfig.widget.BlockStatePropsRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.level.block.state.BlockState;

public class TestScreen extends Screen {
	public BlockState state;
	GuiUtils gui = new GuiUtils();
	BlockStatePropsRenderer renderer;
	public TestScreen(BlockState state) {
		super(CommonComponents.EMPTY);
		this.state = state;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		gui.setGuiGraphics(guiGraphics, this.font, i, j, f);
		renderer.render(gui);
		gui.renderBlockInGui(this.state, null, (float) this.width /2, (float) this.height / 2 - 36, 20);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return renderer.mouseClicked(d, e, i);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		return renderer.mouseScrolled(d, e, g);
	}

	@Override
	protected void init() {
		renderer = new BlockStatePropsRenderer(this.width/2, this.height/2, 5, () -> this.state, (state) -> this.state = state);
	}
}
