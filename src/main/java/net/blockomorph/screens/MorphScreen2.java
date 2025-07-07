package net.blockomorph.screens;

import net.blockomorph.screens.utils.BlocksManager;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.TabManager;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.SavedBlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.function.BiFunction;

public class MorphScreen2 extends Screen {
	private static final ResourceLocation MENU_LOCATION = GuiUtils.res("textures/screens/morph_gui.png");
	private static final ResourceLocation SEARCH_BAR = GuiUtils.res("textures/screens/searchbar.png");
	private static final ResourceLocation MODE_TABS = GuiUtils.res("textures/screens/exit_tabs.png");
	public static final Minecraft mc = Minecraft.getInstance();
	protected final GuiUtils gui = new GuiUtils();
	public static final SavedBlockManager SAVED_BLOCK_MANAGER = new SavedBlockManager(GuiUtils.getSavedBlockManagerPath());
	public final TabManager TAB_MANAGER;
	public final BlocksManager BLOCKS_MANAGER;
	protected static int tabOffset;
	protected PlayerAccessor player;
	public final int imageLength = 176;
	public final int imageHeight = 166;
	private final OnBlockClick onBlockClick;
	private final OnRenderingFrame onRenderingFrame;
	private final BiFunction<Integer, Integer, List<AbstractWidget>> onInit;
	protected int leftPos;
	protected int topPos;


	protected MorphScreen2(OnBlockClick click, OnRenderingFrame frame, BiFunction<Integer, Integer, List<AbstractWidget>> init, boolean useSpecialTabs, boolean useSavedBlocksTab) {
		super(Component.literal("morph_screen"));
		this.player = PlayerAccessor.of(mc.player);
		this.onBlockClick = click;
		this.onRenderingFrame = frame;
		this.onInit = init;
		this.BLOCKS_MANAGER = new BlocksManager(this);
		this.TAB_MANAGER = new TabManager(this, useSpecialTabs, useSavedBlocksTab);
	}

	@ApiStatus.Internal
	public static MorphScreen2 test() {
		return new MorphScreen2((lv, pl, block, number, selectedTab, page) -> {
			return null;
		}, (utils, lv, pl, block, buffer) -> {

		}, (leftPos, topPos) -> List.of(), true, true);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		this.player = PlayerAccessor.of(mc.player);
	}

	public int getLeftPos() {
		return this.leftPos;
	}

	public int getTopPos() {
		return this.topPos;
	}

	public PlayerAccessor getPlayer() {
		return this.player;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, tick);
		this.renderBackground();
		//this.onRenderingFrame.render(this.gui, this.player.player().level(), this.player, );
	}

	private void renderBackground() {
		this.gui.blitMonoImage(MENU_LOCATION, this.leftPos, this.topPos, this.imageLength, this.imageHeight);
		this.gui.blit(MODE_TABS, this.leftPos + 4, this.topPos - 19, 0, 0, 80, 22, 80, 46);
		if (TAB_MANAGER.hasSearchBar()) {
			gui.blitMonoImage(SEARCH_BAR, this.leftPos + 90, this.topPos - 19, 80, 23);
		}
		TAB_MANAGER.renderTabs(this.gui);
		BLOCKS_MANAGER.render(this.gui);
		/* SCROLLER */
	}

	@Override
	public boolean mouseClicked(double x, double y, int type) {
		if (type == 0) {
			if (TAB_MANAGER.mouseClicked(x, y)) {
				return true;
			} else {
				//SoundInstance sound = this.onBlockClick.click(this.player.player().level(), this.player, )
			}
		}
		return false;
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageLength) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		for (AbstractWidget widget : this.onInit.apply(this.leftPos, this.topPos)) {
			this.addRenderableWidget(widget);
		}
		TAB_MANAGER.init(this::addRenderableWidget);
	}
	
	@FunctionalInterface
	public interface OnBlockClick {
		SoundInstance click(Level lv, PlayerAccessor pl, SavedBlock block, int number, CreativeModeTab selectedTab, int page);
	}
	
	@FunctionalInterface
	public interface OnRenderingFrame {
		void render(GuiUtils utils, Level lv, PlayerAccessor pl, SavedBlock block, MultiBufferSource buffer);
	}
}
