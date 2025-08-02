package net.blockomorph.screens.morphConfig.nbtEditor.renderers.interpritationTagRenderers;

import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays.BlockStateSelectorOverlay;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CompoundTagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateTagRenderer extends AbstractInterpritationTagRenderer<CompoundTag> {
	private static final int BUTTON_SIZE = 16;
	private BlockState state;
	private boolean hovered;

	public BlockStateTagRenderer(CompoundTagRenderer parent, TagRendererContext<CompoundTag> ctx) {
		super(parent, ctx);
		this.state = NbtUtils.readBlockState(ctx.provider().lookupOrThrow(Registries.BLOCK), parent.getTag());
	}

	@Override
	public void renderMain(GuiUtils gui) {
		int x = this.box.getX() + this.box.getWidth() - 29 - BUTTON_SIZE;
		int y = this.box.getY() + 2;
		this.hovered = GuiUtils.isMouseOver(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, gui.getMouseX(), gui.getMouseY());
		if (this.hovered)
			gui.blit(TAGS_SPRITE, x, y, PLATE_LENGTH, 46, BUTTON_SIZE, BUTTON_SIZE, PLATE_SPRITE_LENGTH, PLATE_SPRITE_HEIGTH);
		gui.renderBlockInGui(this.state, null, x + BUTTON_SIZE/2f, y + BUTTON_SIZE - 1.5f, 8f, 0);
	}

	@Override
	public boolean mouseClick(double mouseX, double mouseY) {
		if (this.hovered) {
			this.tagRendererContext.onTagEditingRequested().accept(new BlockStateSelectorOverlay(this.state, this::changeBlockState));
			GuiUtils.playClickSound();
			return true;
		}
		return false;
	}

	@Override
	public Component getTooltip(GuiUtils gui) {
		if (this.hovered) {
			return this.state.getBlock().getName();
		}
		return super.getTooltip(gui);
	}

	private void changeBlockState(BlockState state) {
		this.state = state;
		CompoundTag tag = NbtUtils.writeBlockState(state);
		this.changeThis(tag);
	}

	@Override
	protected Integer getPlateNumber() {
		return 5;
	}
}
