package net.blockomorph.screens.morph;

import net.blockomorph.network.ServerBoundBlockMorphPacket;
import net.blockomorph.network.ServerBoundMorphActionPacket;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.SpriteImageButton;
import net.blockomorph.utils.BannedBlock;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class MorphScreen extends AbstractMorphScreen {
	private static final ResourceLocation UNMORPH_BUTTON_SPRITE = GuiUtils.res("textures/screens/unmorph_but.png");
	private static final ResourceLocation FUSE_BUTTON_SPRITE = GuiUtils.res("textures/screens/flame_but.png");
	private static final ResourceLocation LOCK_FRAME = GuiUtils.res("textures/screens/sel_lock.png");
	private static final ResourceLocation SELECTED_FRAME = GuiUtils.res("textures/screens/selected.png");
	private SpriteImageButton fuseButton;

	public MorphScreen(MorphScreenOptions options) {
		super(options);
	}

	@Override
	protected void initAdditional(Consumer<AbstractWidget> action) {
		action.accept(new SpriteImageButton(leftPos + 10, topPos + this.imageHeight + 1, 26, 26, UNMORPH_BUTTON_SPRITE, button -> {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(Blocks.AIR.defaultBlockState(), null));
		}, () -> this.player.isFullActive(), true));

		action.accept(this.fuseButton = new SpriteImageButton(leftPos - 28, topPos + this.imageHeight + 1, 26, 26, FUSE_BUTTON_SPRITE, button -> {
			MorphUtils.sendServer(ServerBoundMorphActionPacket.TNT_ACTION);
		}, () -> this.player.getTnt() == null, true));
		this.fuseButtonVisibilityCheck();
	}

	@Override
	protected void init() {
		super.init();
		this.checkSavedTab();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.fuseButtonVisibilityCheck();
	}

	private void fuseButtonVisibilityCheck() {
		this.fuseButton.visible = this.player.getBlockState(InPlayerBlockPos.ZERO).getBlock() instanceof TntBlock;
	}

	@Nullable
	private BannedBlock isBannedBlock(SavedBlock block) {
		if (!MorphUtils.getScreenAccess(this.player.player()).config && (block.getTag() != null || !block.getState().getBlock().defaultBlockState().equals(block.getState())))
			return new BannedBlock("You cannot morph into configured block!",
					Component.translatable("blockomorph.bannedBlock.configured"));
		return BannedBlock.isBannedBlock(block.getState(), this.player, BannedBlock.Source.NETWORK);
	}

	@Override
	public void onConfigSynced() {
		super.onConfigSynced();
		this.checkSavedTab();
	}

	private void checkSavedTab() {
		if (this.options.useSavedBlocksTab()) {
			CreativeModeTab savedBlocks = TabManager.getTabFromKey(CreativeModeTabs.HOTBAR);
			if (!MorphUtils.getScreenAccess(this.player.player()).config) {
				TAB_MANAGER.SPECIAL_TABS.remove(savedBlocks);
				if (TabManager.getSelectedTab() == savedBlocks) {
					TAB_MANAGER.selectTab(CreativeModeTabs.getDefaultTab());
				}
			} else {
				if (!TAB_MANAGER.SPECIAL_TABS.contains(savedBlocks))
					TAB_MANAGER.SPECIAL_TABS.add(2, savedBlocks);
			}
		}
	}

	@Override
	protected SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page) {
		if (this.isBannedBlock(block) == null) {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(block.getState(), block.getTag()));
			return GuiUtils.getClickSound();
		}
		return null;
	}

	@Override
	protected void renderFrame(SavedBlock block, int x, int y) {
		BlockState state = block.getState();
		BannedBlock ban = this.isBannedBlock(block);
		if (ban != null) {
			gui.blitMonoImage(LOCK_FRAME, x, y, BLOCK_FRAME_SIZE, BLOCK_FRAME_SIZE);
			return;
		}

		BlockState playerState = this.player.getBlockState(InPlayerBlockPos.ZERO);
		CompoundTag tg = this.player.getTag(InPlayerBlockPos.ZERO);

		if (state.equals(playerState)) {
			if (block.getTag() == null || tg.equals(block.getTag())) {
				gui.blitMonoImage(SELECTED_FRAME, x, y, BLOCK_FRAME_SIZE, BLOCK_FRAME_SIZE);
			}
		}
	}

	@Override
	protected void renderTooltip() {
		SavedBlock block = BLOCKS_MANAGER.getBlockAtPosition(gui.getMouseX(), gui.getMouseY());
		if (block != null) {
			BannedBlock ban = this.isBannedBlock(block);
			if (ban != null) {
				Component name = block.getName() == null ? block.getState().getBlock().getName() : Component.literal(block.getName());
				List<Component> hints = List.of(name, Component.literal(ChatFormatting.RED + ban.text().getString()));
				gui.renderTooltip(hints, gui.getMouseX(), gui.getMouseY());
			} else {
				super.renderTooltip();
			}
		}
	}
}
