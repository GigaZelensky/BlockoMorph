package net.blockomorph.screens.morph;

import net.blockomorph.screens.AbstractScreen;
import net.blockomorph.screens.morphConfig.MorphConfigScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ScrollerManager;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.SavedBlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.function.Consumer;

public abstract class AbstractMorphScreen extends AbstractScreen implements ConfigSyncListener {
	private static final ResourceLocation SEARCH_BAR = GuiUtils.res("textures/screens/searchbar.png");
	private static final ResourceLocation MODE_TABS = GuiUtils.res("textures/screens/exit_tabs.png");
	public static final SavedBlockManager SAVED_BLOCK_MANAGER = new SavedBlockManager(MorphUtils.getGameDir());
	protected static final int BLOCK_FRAME_SIZE = 36;
	public final TabManager TAB_MANAGER;
	public final BlocksManager BLOCKS_MANAGER;
	protected PlayerAccessor player;
	protected final MorphScreenOptions options;
	protected boolean ignoreSearchBoxInput;


	protected AbstractMorphScreen(MorphScreenOptions options) {
		super("morph_screen", null);
		this.options = options;
		this.player = PlayerAccessor.of(GuiUtils.MC.player);
		this.BLOCKS_MANAGER = new BlocksManager(this);
		this.TAB_MANAGER = new TabManager(this, options.useAllowedTab(), options.useSavedBlocksTab());
	}

	protected abstract void initAdditional(Consumer<AbstractWidget> action);

	protected abstract void renderFrame(SavedBlock block, int x, int y);

	protected abstract SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page);

	protected void renderTooltip() {
		SavedBlock block = BLOCKS_MANAGER.getBlockAtPosition(gui.getMouseX(), gui.getMouseY());
		if (block != null) {
			Component name = block.getName() == null ? block.getState().getBlock().getName() : Component.literal(block.getName());
			gui.renderTooltip(name, gui.getMouseX(), gui.getMouseY());
		}
	}

	@Override
	public void tick() {
		this.player = PlayerAccessor.of(GuiUtils.MC.player);
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
		this.renderContent();
		this.renderTooltip();
	}

	private void renderContent() {
		TAB_MANAGER.renderTabs(this.gui);
		BLOCKS_MANAGER.render(this.gui, this::renderFrame);
	}

	@Override
	public void renderMenu() {
		super.renderMenu();
		if (this.options.useUpperTabs()) {
			this.gui.blit(MODE_TABS, this.leftPos + 4, this.topPos - 19, 0, 0, 80, 22, 80, 46);
		}
		if (TAB_MANAGER.hasSearchBar()) {
			gui.blitMonoImage(SEARCH_BAR, this.leftPos + 90, this.topPos - 19, 80, 23);
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int type) {
		if (type == 0) {
			if (TAB_MANAGER.mouseClicked(x, y)) {
				return true;
			} else if (BLOCKS_MANAGER.mouseClicked(x, y, this::onClickOnBlock)) {
				return true;
			} else if (this.options.useUpperTabs) {
				if (x > this.leftPos + 4 && x < this.leftPos + 4 + 41 && y > this.topPos - 19 && y < this.topPos - 19 + 22) {
					GuiUtils.MC.setScreen(new MorphConfigScreen(true)); //TODO
					return true;
				}
			}
		}
		return super.mouseClicked(x, y, type);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int type, double mouseXOffset, double mouseYOffset) {
		if (BLOCKS_MANAGER.scrollerManager.mouseDragged(mouseY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
	}

	@Override
	public boolean mouseReleased(double x, double y, int type) {
		if (type == 0) {
			if (TAB_MANAGER.mouseClicked(x, y)) {
				return true;
			} else BLOCKS_MANAGER.scrollerManager.disableScrollWork();
		}
		return super.mouseReleased(x, y, type);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double xScrolled, double yScrolled) {
		if (BLOCKS_MANAGER.scrollerManager.mouseScrolled(yScrolled)) {
			return true;
		}
		return super.mouseScrolled(x, y, xScrolled, yScrolled);
	}

	public AbstractMorphScreen ignoreInitInput() {
		this.ignoreSearchBoxInput = true;
		return this;
	}

	@Override
	public boolean charTyped(char codePoint, int mods) {
		if (this.ignoreSearchBoxInput) {
			this.ignoreSearchBoxInput = false;
			return false;
		} else if (TAB_MANAGER.getSearchBox().charTyped(codePoint, mods)) {
			return true;
		}
		return super.charTyped(codePoint, mods);
	}

	@Override
	public boolean keyPressed(int keyboardButton, int scanCode, int mods) {
		if (keyboardButton == 256) {
			this.onClose();
		} else {
			if (TAB_MANAGER.getSearchBox().keyPressed(keyboardButton, scanCode, mods)) {
				return true;
			}
		}
		return super.keyPressed(keyboardButton, scanCode, mods);
	}

	@Override
	protected void init() {
		super.init();
		this.initAdditional(this::addRenderableWidget);
		TAB_MANAGER.init(this::addRenderableWidget);
		if (!TAB_MANAGER.SPECIAL_TABS.contains(TabManager.selectedTab)) {
			TabManager.selectedTab = CreativeModeTabs.getDefaultTab();
		}
		TAB_MANAGER.selectTab(TabManager.getSelectedTab());
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		ScrollerManager<SavedBlock> manager = BLOCKS_MANAGER.scrollerManager;
		float scroll = manager.getScrollerOffset();
		String value = TAB_MANAGER.getSearchBox().getValue();
		super.resize(minecraft, width, height);
		TAB_MANAGER.getSearchBox().setValue(value);
		BLOCKS_MANAGER.searchBlocks(value);
		manager.setScrollOffset(scroll);
		manager.refreshList();
	}
	
	@FunctionalInterface
	public interface OnBlockClick {
		SoundInstance click(SavedBlock block, int number, CreativeModeTab selectedTab, int page);
	}
	
	@FunctionalInterface
	public interface OnRenderingFrame {
		void render(SavedBlock block, int x, int y);
	}

	@Override
	public void onConfigSynced() {
		BLOCKS_MANAGER.updateAllowedBlocks();
	}

	public record MorphScreenOptions(boolean useAllowedTab, boolean useSavedBlocksTab, boolean useUpperTabs) {
		public static MorphScreenOptions ALL = new MorphScreenOptions(true, true, true);
		public static MorphScreenOptions CONFIG = new MorphScreenOptions(false, true, false);
		public static MorphScreenOptions OFF = new MorphScreenOptions(false, false, false);
	}
}
