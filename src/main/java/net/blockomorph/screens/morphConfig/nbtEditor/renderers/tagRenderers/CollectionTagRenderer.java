package net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers;

import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.primitive.NumericTagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.nbt.*;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.List;

public class CollectionTagRenderer<LIST extends CollectionTag> extends TagRenderer<LIST> {
	private static final int BUTTON_SIZE = 16;
	private boolean hovered;
	private final int color;
	
	protected CollectionTagRenderer(String tagName, LIST tag, TagRendererContext<LIST> ctx, int color) {
		super(tagName, tag, ctx);
		this.color = color;
	}

	@Override
	protected void renderPlate(GuiUtils gui, int number) {
		gui.fill(this.box.getX(), this.box.getY(), this.box.getX() + this.box.getWidth(), this.box.getY() + this.box.getHeight(), this.color);
		super.renderPlate(gui, number);
	}

	@Override
	public void render(GuiUtils gui) {
		int x = this.box.getX() + (this.hasName() ? MAX_NAME_WIDTH + 8 : this.box.getWidth() - 3 - BUTTON_SIZE);
		int y = this.box.getY() + 2;
		this.hovered = GuiUtils.isMouseOver(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, gui.getMouseX(), gui.getMouseY());
		gui.blit(TAGS_SPRITE, x, y, PLATE_LENGTH + BUTTON_SIZE, this.hovered ? BUTTON_SIZE : 0, BUTTON_SIZE, BUTTON_SIZE, PLATE_SPRITE_LENGTH, PLATE_SPRITE_HEIGTH);
	}

	@Override
	protected void renderLine(GuiUtils gui, int number) {
		this.renderDynamicColorLine(gui);
	}

	@Override
	public boolean canEnterInTag() {
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY) {
		if (this.hovered) {
			this.enterInTag();
			GuiUtils.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	protected Integer getPlateNumber() {
		return 3;
	}

	@Override
	public int getFrameColor() {
		return this.color;
	}

	@Override
	public List<TagRenderer<?>> getEnteringTags() {
		List<TagRenderer<?>> renderers = new ArrayList<>();
		int i = 1;
		for (Tag tag : this.getTag()) {
			if (tag == null) continue;
			final int index = i;
			TagRenderer<?> renderer = NbtEditorScreen.getRendererForTag(index + "", tag, this.tagRendererContext.withTagUpdateListener(newTag -> {
				this.getTag().setTag(index - 1, newTag);
			}));
			if (renderer != null) renderer.setNameVisibility(false);
			renderers.add(renderer);
			i++;
		}
		return renderers;
	}

	@Override
	public Tag tryWalk(String nameElementInThisTag) {
		try {
			int index = Integer.parseInt(nameElementInThisTag) - 1;
			return this.getTag().get(index);
		} catch (Exception e) {
			return null;
		}
	}

	public static final class ListTagRenderer extends CollectionTagRenderer<ListTag> {
		public ListTagRenderer(String tagName, ListTag tag, TagRendererContext<ListTag> ctx) {
			super(tagName, tag, ctx, ARGB.color(255, 245, 172, 47));
		}
	}

	public static final class IntArrayTagRenderer extends CollectionTagRenderer<IntArrayTag> {
		public IntArrayTagRenderer(String tagName, IntArrayTag tag, TagRendererContext<IntArrayTag> ctx) {
			super(tagName, tag, ctx, NumericTagRenderer.INT_COLOR);
		}
	}

	public static final class LongArrayTagRenderer extends CollectionTagRenderer<LongArrayTag> {
		public LongArrayTagRenderer(String tagName, LongArrayTag tag, TagRendererContext<LongArrayTag> ctx) {
			super(tagName, tag, ctx, NumericTagRenderer.LONG_COLOR);
		}
	}

	public static final class ByteArrayTagRenderer extends CollectionTagRenderer<ByteArrayTag> {
		public ByteArrayTagRenderer(String tagName, ByteArrayTag tag, TagRendererContext<ByteArrayTag> ctx) {
			super(tagName, tag, ctx, NumericTagRenderer.BYTE_COLOR);
		}
	}
}
