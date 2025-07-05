package net.blockomorph.screens;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.TabManager;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MorphScreen2 extends Screen {
	private static final ResourceLocation MENU_LOCATION = GuiUtils.res("textures/screens/morph_gui.png");
	private static final ResourceLocation SEARCH_BAR = GuiUtils.res("textures/screens/searchbar.png");
	private static final ResourceLocation MODE_TABS = GuiUtils.res("textures/screens/exit_tabs.png");
	protected final Minecraft mc = Minecraft.getInstance();
	protected final MultiBufferSource bufferSource = mc.renderBuffers().bufferSource();
	protected final GuiUtils gui = new GuiUtils();
	protected final TabManager TAB_MANAGER;
	protected static int tabOffset;
	protected PlayerAccessor player;
	public final int imageLength = 176;
	public final int imageHeight = 166;
	private final OnBlockClick onBlockClick;
	private final OnRenderingFrame onRenderingFrame;
	private final Runnable onInit;
	protected int leftPos;
	protected int topPos;


	protected MorphScreen2(OnBlockClick click, OnRenderingFrame frame, Runnable init, boolean useSpecialTabs, boolean useSavedBlocksTab) {
		super(Component.literal("morph_screen"));
		this.onBlockClick = click;
		this.onRenderingFrame = frame;
		this.onInit = init;
		this.TAB_MANAGER = new TabManager(this, useSpecialTabs, useSavedBlocksTab);
		this.player = PlayerAccessor.of(mc.player);
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

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.gui.setGuiGraphics(guiGraphics, this.font);
		this.renderBackground(mouseX, mouseY);
	}

	private void renderBackground(int mouseX, int mouseY) {
		this.gui.blitMonoImage(MENU_LOCATION, this.leftPos, this.topPos, this.imageLength, this.imageHeight);
		this.gui.blit(MODE_TABS, this.leftPos + 4, this.topPos - 19, 0, 0, 80, 22, 80, 46);
		TAB_MANAGER.renderTabs(this.gui, mouseX, mouseY);
		/* SCROLLER */
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageLength) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		this.onInit.run();
		TAB_MANAGER.init(this::addRenderableWidget);
	}
	
	@FunctionalInterface
	public interface OnBlockClick {
		void click(Level lv, PlayerAccessor pl, SavedBlock block, int number, CreativeModeTab selectedTab, int page);
	}
	
	@FunctionalInterface
	public interface OnRenderingFrame {
		void render(GuiUtils utils, Level lv, PlayerAccessor pl, SavedBlock block, int mouseX, int mouseY, float tick, MultiBufferSource buffer);
	}
}
