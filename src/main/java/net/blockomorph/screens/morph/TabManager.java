package net.blockomorph.screens.morph;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ListenerEditBox;
import net.blockomorph.screens.utils.ScrollerManager;
import net.blockomorph.utils.BannedBlock;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TabManager {
	protected static final CreativeModeTab ALLOWED_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("blockomorph.gui.morphScreen.allowed_tab")).icon(() -> new ItemStack(Items.NETHER_STAR)).build();
	private static final ResourceLocation TABS_SPRITE = GuiUtils.res("textures/screens/block_selector_tabs.png");
	protected final AbstractMorphScreen parentScreen;
	protected final List<CreativeModeTab> CONTENT_TABS;
	protected final List<CreativeModeTab> SPECIAL_TABS;
	protected static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
	protected static int tabPage = 0;
	protected final int pageCount;
	protected EditBox searchBox;
	protected final boolean needAllowedTab;

	public TabManager(AbstractMorphScreen screen, boolean useAllowedTab, boolean useSavedBlocksTab) {
		this.parentScreen = screen;
		this.needAllowedTab = useAllowedTab;
		AbstractMorphScreen.SAVED_BLOCK_MANAGER.load();
		this.initTabs();
		List<CreativeModeTab> list = new ArrayList<>();
		list.add(CreativeModeTabs.searchTab());
		list.add(getTabFromKey(CreativeModeTabs.OP_BLOCKS));
		if (useSavedBlocksTab) list.add(getTabFromKey(CreativeModeTabs.HOTBAR));
		SPECIAL_TABS = list;
		CONTENT_TABS = screen.BLOCKS_MANAGER.sortTabsIfItemsIsBlocks();
		this.putAllowedTabIfNeed();
		this.pageCount = (int) Math.ceil((double) CONTENT_TABS.size() / 10);
	}

	public void renderTabs(GuiUtils gui) {
		this.searchBox.visible = this.hasSearchBar();
		this.renderTabsInGui(gui);
		CreativeModeTab tab = this.getTabAtPosition(gui.getMouseX(), gui.getMouseY());
		if (tab != null) gui.renderTooltip(tab.getDisplayName(), gui.getMouseX(), gui.getMouseY());
		if (selectedTab.showTitle())
			gui.drawString(selectedTab.getDisplayName(), parentScreen.getLeftPos() + 8, parentScreen.getTopPos() + 6, 0x404040, false);
		if (this.pageCount > 1) {
			Component pageCounter = Component.literal(String.format("%d / %d", tabPage + 1, this.pageCount));
			gui.drawString(pageCounter, parentScreen.getLeftPos() + (parentScreen.imageLength / 2) - (gui.getFont().width(pageCounter) / 2), parentScreen.getTopPos() - 34, -1, true);
		}
	}

	public boolean hasSearchBar() {
		return selectedTab == CreativeModeTabs.searchTab() || selectedTab == ALLOWED_TAB || selectedTab == getTabFromKey(CreativeModeTabs.HOTBAR);
	}

	public EditBox getSearchBox() {
		return this.searchBox;
	}

	public static CreativeModeTab getSelectedTab() {
		return selectedTab;
	}

	public static int getTabPage() {
		return tabPage;
	}

	public void init(Consumer<AbstractWidget> action) {
		if (this.pageCount > 1) {
			int leftPos = parentScreen.getLeftPos();
			int topPos = parentScreen.getTopPos();
			action.accept(Button.builder(Component.literal("<"), b -> this.setPage(false)).pos(leftPos - 22,  topPos - 22).size(20, 20).build());
			action.accept(Button.builder(Component.literal(">"), b -> this.setPage(true)).pos(leftPos + parentScreen.imageLength, topPos - 22).size(20, 20).build());
		}
		this.searchBox = new ListenerEditBox(this.parentScreen.getFont(), this.parentScreen.getLeftPos() + 99, this.parentScreen.getTopPos() - 10, 70, 12, Component.translatable("itemGroup.search"), parentScreen.BLOCKS_MANAGER::searchBlocks, null);
		this.searchBox.setMaxLength(32767);
		this.searchBox.setBordered(false);
		this.searchBox.setTextColor(16777215);
		action.accept(this.searchBox);
	}

	public boolean mouseClicked(double x, double y) {
		CreativeModeTab tab = this.getTabAtPosition(x, y);
		if (tab != null) {
			return this.selectTab(tab);
		}
		return false;
	}

	public boolean selectTab(CreativeModeTab tab) {
		List<SavedBlock> list = BlocksManager.ALL_TAB_CONTENTS.get(getKeyFromTab(tab));
		if (list != null) {
			CreativeModeTab old = selectedTab;
			selectedTab = tab;
			ScrollerManager<SavedBlock> manager = parentScreen.BLOCKS_MANAGER.scrollerManager;
			if (old != tab) manager.setScrollOffset(0f);
			manager.setMainList(list);
			manager.refreshList();
			this.searchBox.setValue("");
			boolean flag = this.hasSearchBar();
			this.searchBox.visible = flag;
			this.searchBox.setCanLoseFocus(!flag);
			this.searchBox.setFocused(flag);
			return true;
		}
		return false;
	}

	private void initTabs() {
		LocalPlayer player = ((LocalPlayer)parentScreen.getPlayer());
		CreativeModeTabs.tryRebuildTabContents(
				player.connection.enabledFeatures(),
				true,
				player.level().registryAccess());
	}

	private void setPage(boolean up) {
		tabPage = up ? Math.min(tabPage + 1, pageCount - 1) : Math.max(tabPage - 1, 0);
	}

	protected boolean isSelected(boolean special, int i) {
		return (special ? SPECIAL_TABS : CONTENT_TABS).get(i) == selectedTab;
	}

	protected void putAllowedTabIfNeed() {
		List<SavedBlock> blocks = BlocksManager.ALL_TAB_CONTENTS.get(getKeyFromTab(ALLOWED_TAB));
		if (blocks != null) {
			if (!this.needAllowedTab) {
				SPECIAL_TABS.remove(ALLOWED_TAB);
			} else {
				List<SavedBlock> filteredBlocks = blocks.stream().filter(savedBlock -> {
					BannedBlock reason = BannedBlock.isBannedBlock(savedBlock.getState(), this.parentScreen.player, BannedBlock.Source.SYSTEM);
					return reason == null || reason.systemLock();
				}).toList();
				if (filteredBlocks.size() != BlocksManager.ALL_BLOCKS.size()) {
					if (!SPECIAL_TABS.contains(ALLOWED_TAB))
						SPECIAL_TABS.add(ALLOWED_TAB);
				} else {
					SPECIAL_TABS.remove(ALLOWED_TAB);
				}
			}
		}
	}
	
	protected void renderTabsInGui(GuiUtils gui) {
		this.renderContentTabs(gui);
		this.renderSpecialTabs(gui);
	}

	protected void renderSpecialTabs(GuiUtils gui) {
		for (int i = 0; i < SPECIAL_TABS.size(); i++) {
			int tabXSpecial = parentScreen.getLeftPos() + parentScreen.imageLength - 38 - i * 32;
			gui.blit(TABS_SPRITE, tabXSpecial, this.getTabY(-1), this.isSelected(true, i) ? 28 : 0, 0, 28, 32, 64, 88);
			this.renderItemInTab(gui, null, i, -1);
		}
	}

	protected void renderContentTabs(GuiUtils gui) {
		int count = 0;
		for (int i = tabPage * 10; i < tabPage * 10 + 10; i++) {
			if (i < CONTENT_TABS.size()) {
				boolean isRight = count >= 5;
				//String selectedWord = (this.isSelected(false, i) ? "_selected" : "");
				int tabXSpecial = parentScreen.getLeftPos() + (isRight ? parentScreen.imageLength - 4 : -28);
				//gui.blitMonoImage(TAB_LOCATION.apply((isRight ? "right" : "left") + "_middle" + selectedWord), tabXSpecial, this.getTabY(count), 32, 28);
				gui.blit(TABS_SPRITE, tabXSpecial, this.getTabY(count), isRight ? 32 : 0, this.isSelected(false, i) ? 60 : 32, 32, 28, 64, 88);
				this.renderItemInTab(gui, isRight, i, count);
				count++;
			} else break;
		}
	}

	protected void renderItemInTab(GuiUtils gui, @Nullable Boolean isRight, int listIndex, int offsetIndex) {
		float tabX;
		boolean isDown = isRight == null;
		if (isDown) {
			tabX = parentScreen.getLeftPos() + parentScreen.imageLength - 37 - listIndex * 32 + 5;
		} else {
			tabX = parentScreen.getLeftPos() + (isRight ? parentScreen.imageLength + 2 : -19);
		}
		int tabY = this.getTabY(offsetIndex) + (isDown ? 7 : 5);

		ItemStack itemstack = (isDown ? SPECIAL_TABS : CONTENT_TABS).get(listIndex).getIconItem();
		gui.renderItem(itemstack, tabX, tabY, 1f, 100);
	}

	protected CreativeModeTab getTabAtPosition(double x, double y) {
		int leftPos = parentScreen.getLeftPos();
		int imageWidth = parentScreen.imageLength;

		int tabYSpecial = this.getTabY(-1);
		for (int i = 0; i < SPECIAL_TABS.size(); i++) {
			int tabXSpecial = leftPos + imageWidth - 38 - i * 32;
			if (x > tabXSpecial && x < tabXSpecial + 28 && y > tabYSpecial && y < tabYSpecial + 32) {
				return SPECIAL_TABS.get(i);
			}
		}
		for (int i = 0; i < 10; i++) {

			int tabX = leftPos;
			int tabY = this.getTabY(i);

			if (i < 5) {
				tabX -= 28;
			} else {
				tabX += imageWidth - 4;
			}
			if (x > tabX && x < tabX + 32 && y > tabY && y < tabY + 28) {
				if (10 * tabPage + i < CONTENT_TABS.size())
					return CONTENT_TABS.get(10 * tabPage + i);
			}

		}

		return null;
	}
	
	public static CreativeModeTab getTabFromKey(ResourceKey<CreativeModeTab> name) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(name);
	}

	public static ResourceKey<CreativeModeTab> getKeyFromTab(CreativeModeTab tab) {
		if (tab == ALLOWED_TAB) {
			return BlocksManager.ALLOWED_TAB_KEY;
		}
		return BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElseThrow();
	}

	protected int getTabY(int i) {
		if (i < 0) return parentScreen.getTopPos() + parentScreen.imageHeight - 4; //Special Tabs

		if (i > 4) i -= 5; //right column

		return parentScreen.getTopPos() + 3 + i * 32;
	}
}
