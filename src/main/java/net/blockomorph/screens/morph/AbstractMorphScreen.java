package net.blockomorph.screens.morph;

import net.blockomorph.screens.BlockMorphConfigScreen;
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

import java.util.function.Consumer;

public abstract class AbstractMorphScreen extends Screen {
	private static final ResourceLocation MENU_LOCATION = GuiUtils.res("textures/screens/morph_gui.png");
	private static final ResourceLocation SEARCH_BAR = GuiUtils.res("textures/screens/searchbar.png");
	private static final ResourceLocation MODE_TABS = GuiUtils.res("textures/screens/exit_tabs.png");
	public static final Minecraft mc = GuiUtils.MC;
	protected final GuiUtils gui = new GuiUtils();
	public static final SavedBlockManager SAVED_BLOCK_MANAGER = new SavedBlockManager(MorphUtils.getGameDir());
	public final TabManager TAB_MANAGER;
	public final BlocksManager BLOCKS_MANAGER;
	protected PlayerAccessor player;
	public final int imageLength = 176;
	public final int imageHeight = 166;
	private final MorphScreenOptions options;
	protected int leftPos;
	protected int topPos;
	protected boolean ignoreSearchBoxInput;


	protected AbstractMorphScreen(MorphScreenOptions options) {
		super(Component.literal("morph_screen"));
		this.options = options;
		this.player = PlayerAccessor.of(mc.player);
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
		this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, tick);
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.renderContent();
		this.renderTooltip();
	}

	private void renderContent() {
		TAB_MANAGER.renderTabs(this.gui);
		BLOCKS_MANAGER.render(this.gui, this::renderFrame);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, tick);
		this.gui.blitMonoImage(MENU_LOCATION, this.leftPos, this.topPos, this.imageLength, this.imageHeight);
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
					mc.setScreen(new BlockMorphConfigScreen(true)); //TODO
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
		this.leftPos = (this.width - this.imageLength) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		this.initAdditional(this::addRenderableWidget);
		TAB_MANAGER.init(this::addRenderableWidget);
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

	public record MorphScreenOptions(boolean useAllowedTab, boolean useSavedBlocksTab, boolean useUpperTabs) {
		public static MorphScreenOptions ALL = new MorphScreenOptions(true, true, true);
		public static MorphScreenOptions CONFIG = new MorphScreenOptions(false, true, false);
	}
}
