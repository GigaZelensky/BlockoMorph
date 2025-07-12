package net.blockomorph.screens;

import net.blockomorph.network.ServerBoundBlockMorphPacket;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.SpriteImageButton;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

public class MorphScreen extends AbstractMorphScreen {
	private static final ResourceLocation UNMORPH_BUTTON_SPRITE = GuiUtils.res("textures/screens/unmorph_but.png");
	private static final ResourceLocation FUSE_BUTTON_SPRITE = GuiUtils.res("textures/screens/flame_but.png");
	private static final ResourceLocation LOCK_FRAME = GuiUtils.res("textures/screens/sel_lock.png");
	private static final ResourceLocation SELECTED_FRAME = GuiUtils.res("textures/screens/selected.png");
	private SpriteImageButton fuseButton;

	public MorphScreen() {
		super(MorphScreenOptions.ALL);
	}

	@Override
	protected void initAdditional(Consumer<AbstractWidget> action) {
		action.accept(new SpriteImageButton(leftPos + 10, topPos + this.imageHeight + 1, 26, 26, UNMORPH_BUTTON_SPRITE, button -> {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(Blocks.AIR.defaultBlockState(), null));
		}, () -> this.player.isFullActive(), true));

		action.accept(this.fuseButton = new SpriteImageButton(leftPos - 28, topPos + this.imageHeight + 1, 26, 26, FUSE_BUTTON_SPRITE, button -> {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.fuse());
		}, () -> this.player.getTnt() == null, true));
		this.fuseButtonVisibilityCheck();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.fuseButtonVisibilityCheck();
	}

	private void fuseButtonVisibilityCheck() {
		this.fuseButton.visible = this.player.getBlockState(InPlayerBlockPos.ZERO).getBlock() instanceof TntBlock;
	}

	@Override
	protected SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page) {
		if (MorphUtils.isBannedBlock(block.getState(), this.player.player()) == null) {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(block.getState(), block.getTag()));
			return SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
		}
		return null;
	}

	@Override
	protected void renderFrame(SavedBlock block, int x, int y) {
		BlockState state = block.getState();
		MorphUtils.BannedBlock ban = MorphUtils.isBannedBlock(state, this.player.player());
		if (ban != null) {
			gui.blitMonoImage(LOCK_FRAME, x, y, 36, 36);
			return;
		}

		BlockState playerState = this.player.getBlockState(InPlayerBlockPos.ZERO);
		CompoundTag tg = this.player.getTag(InPlayerBlockPos.ZERO);

		if (state.equals(playerState)) {
			if (block.getTag() == null || tg.equals(block.getTag())) {
				gui.blitMonoImage(SELECTED_FRAME, x, y, 36, 36);
			}
		}
	}

	@Override
	protected void renderTooltip() {
		SavedBlock block = BLOCKS_MANAGER.getBlockAtPosition(gui.getMouseX(), gui.getMouseY());
		if (block != null) {
			MorphUtils.BannedBlock ban = MorphUtils.isBannedBlock(block.getState(), this.player.player());
			if (ban != null) {
				List<Component> hints = List.of(
						block.getState().getBlock().getName(),
						Component.literal(ChatFormatting.RED + ban.text().getString())
				);
				gui.renderTooltip(hints, gui.getMouseX(), gui.getMouseY());
			} else {
				super.renderTooltip();
			}
		}
	}
}
