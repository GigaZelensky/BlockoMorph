package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;
import java.util.function.Consumer;

public class ScreenAdapterOverlay<SC extends Screen> extends TagEditingOverlay {
	protected final SC screen;

	public ScreenAdapterOverlay(SC screen) {
		super(Objects.requireNonNull(screen).getClass().getName() + " overlay");
		this.screen = screen;
	}

	@Override
	public void init(int width, int height, Consumer<TagEditingOverlay> onChange) {
		this.screen.init(GuiUtils.MC, width, height);
	}

	@Override
	public void renderInGui(GuiUtils gui) {
		this.screen.renderWithTooltip(gui.getGuiGraphics(), gui.getMouseX(), gui.getMouseY(), gui.getTick());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		return this.screen.mouseClicked(mouseX, mouseY, type);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		return this.screen.mouseScrolled(mouseX, mouseY, xWheelOffset, yWheelOffset);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int type, double mouseXOffset, double mouseYOffset) {
		return this.screen.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int type) {
		return this.screen.mouseReleased(mouseX, mouseY, type);
	}

	@Override
	public boolean charTyped(char character, int mods) {
		return this.screen.charTyped(character, mods);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		return this.screen.keyPressed(key, scancode, mods);
	}
}
