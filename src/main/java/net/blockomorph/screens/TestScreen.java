package net.blockomorph.screens;

import net.blockomorph.screens.utils.EnumListRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.properties.RailShape;

import java.util.List;

public class TestScreen extends Screen {
	private EnumListRenderer renderer;
	private int x;
	private int y;
	private Enum<?> value = Config.Mode.NONE;
	public TestScreen() {
		super(CommonComponents.EMPTY);
	}

	@Override
	protected void init() {
		renderer = new EnumListRenderer(-1, ChatFormatting.YELLOW.getColor(), 7);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		GuiUtils gui = new GuiUtils();
		gui.setGuiGraphics(guiGraphics, this.font, i, j, f);
		this.x = this.width / 2;
		this.y  = this.height / 2;
		renderer.render(gui, this.x, this.y, - 2,11);
		renderer.renderName(gui, this.x, this.y, value, -1);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (GuiUtils.isMouseOver(this.x, this.y, this.x + 17, this.y + 11, d, e)) {
			renderer.drop(List.of(RailShape.values()), (value) -> {
				this.value = value;
			});
		}
		return renderer.mouseClicked(d, e);
	}

	@Override
	public boolean mouseScrolled(double d, double e, double f, double g) {
		return renderer.mouseScrolled(d, e, g);
	}
}
