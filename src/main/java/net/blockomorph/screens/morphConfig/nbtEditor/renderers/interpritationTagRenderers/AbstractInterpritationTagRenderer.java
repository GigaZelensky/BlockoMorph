package net.blockomorph.screens.morphConfig.nbtEditor.renderers.interpritationTagRenderers;

import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractInterpritationTagRenderer<T extends Tag> extends TagRenderer<T> {
	protected static final int BUTTON_SIZE = 7;
	protected boolean hovered;
	protected final Runnable onInterpretationBrake;

	public AbstractInterpritationTagRenderer(String tagName, T tag, TagRendererContext<T> ctx) {
		super(tagName, tag, ctx);
		this.onInterpretationBrake = ctx.onInterpretationBrake();
	}

	@Override
	public final void render(GuiUtils gui) {
		int x = this.box.getX() + this.box.getWidth() - BUTTON_SIZE - 2;
		int y = this.box.getY() + 2;
		this.hovered = GuiUtils.isMouseOver(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, gui.getMouseX(), gui.getMouseY());
		gui.blit(TAGS_SPRITE, x, y, PLATE_LENGTH + 24, this.hovered ? 39 : 32, BUTTON_SIZE, BUTTON_SIZE, PLATE_SPRITE_LENGTH, PLATE_SPRITE_HEIGTH);
		this.renderMain(gui);
	}

	public abstract void renderMain(GuiUtils gui);

	@Override
	public final boolean mouseClicked(double mouseX, double mouseY) {
		if (this.hovered) {
			GuiUtils.playClickSound();
			this.onInterpretationBrake.run();
			return true;
		}
		return this.mouseClick(mouseX, mouseY);
	}

	public abstract boolean mouseClick(double mouseX, double mouseY);

	@Override @Nullable
	public final AbstractInterpritationTagRenderer<T> getInterpretationRenderer(Runnable onInterpretationBrake) {
		return null;
	}
}
