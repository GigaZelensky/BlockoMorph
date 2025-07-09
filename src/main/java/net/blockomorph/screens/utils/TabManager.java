package net.blockomorph.screens.utils;

import com.google.common.collect.ImmutableList;
import net.blockomorph.screens.MorphScreen2;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.SessionSearchTrees;
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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TabManager {
	protected static final CreativeModeTab ALLOWED_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("gui.blockomorph.allowedBlocks")).icon(() -> new ItemStack(Items.NETHER_STAR)).build();
	protected final MorphScreen2 parentScreen;
	protected final List<CreativeModeTab> CONTENT_TABS;
	protected static final Function<String, ResourceLocation> TAB_LOCATION = (tabName) -> GuiUtils.vanillaRes("textures/gui/sprites/advancements/tab_" + tabName+ ".png");
	protected final List<CreativeModeTab> SPECIAL_TABS;
	protected static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
	protected static int tabPage = 0;
	protected final int pageCount;

	public TabManager(MorphScreen2 screen, boolean useAllowedTab, boolean useSavedBlocksTab) {
		this.parentScreen = screen;
		MorphScreen2.SAVED_BLOCK_MANAGER.load();
		this.initTabs();
		ImmutableList.Builder<CreativeModeTab> list = ImmutableList.builder();
		list.add(CreativeModeTabs.searchTab());
		list.add(getTabFromKey(CreativeModeTabs.OP_BLOCKS));
		if (useSavedBlocksTab) list.add(getTabFromKey(CreativeModeTabs.HOTBAR));
		if (useAllowedTab) list.add(ALLOWED_TAB);
		SPECIAL_TABS = list.build();
		CONTENT_TABS = screen.BLOCKS_MANAGER.sortTabsIfItemsIsBlocks();
		this.pageCount = (int) Math.ceil((double) CONTENT_TABS.size() / 10);
	}

	public void renderTabs(GuiUtils gui) {
		this.renderTabsInGui(gui);
		CreativeModeTab tab = this.getTabAtPosition(gui.getMouseX(), gui.getMouseY());
		if (tab != null) gui.renderTooltip(tab.getDisplayName(), gui.getMouseX(), gui.getMouseY());
		if (selectedTab.showTitle())
			gui.getGuiGraphics().drawString(gui.getFont(), selectedTab.getDisplayName(), parentScreen.getLeftPos() + 8, parentScreen.getTopPos() + 6, 0x404040, false);
	}

	public boolean hasSearchBar() {
		return selectedTab == CreativeModeTabs.searchTab() || selectedTab == ALLOWED_TAB || selectedTab == getTabFromKey(CreativeModeTabs.HOTBAR);
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
		if (list != null && !list.isEmpty()) {
			selectedTab = tab;
			ScrollerManager<SavedBlock> manager = parentScreen.BLOCKS_MANAGER.scrollerManager;
			manager.setScrollOffset(0f);
			manager.setMainList(list);
			manager.refreshList();
			return true;
		}
		return false;
	}

	private void initTabs() {
		LocalPlayer player = ((LocalPlayer)parentScreen.getPlayer());
		if (CreativeModeTabs.tryRebuildTabContents(
				player.connection.enabledFeatures(),
				true,
				player.level().registryAccess())
		) {
			SessionSearchTrees sessionSearchTrees = player.connection.searchTrees();
			List<ItemStack> list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
			sessionSearchTrees.updateCreativeTooltips(player.level().registryAccess(), list);
			sessionSearchTrees.updateCreativeTags(list);
		}
	}

	private void setPage(boolean up) {
		tabPage = up ? Math.min(tabPage + 1, pageCount - 1) : Math.max(tabPage - 1, 0);
	}

	protected boolean isSelected(boolean special, int i) {
		return (special ? SPECIAL_TABS : CONTENT_TABS).get(i) == selectedTab;
	}
	
	protected void renderTabsInGui(GuiUtils gui) {
		this.renderContentTabs(gui);
		this.renderSpecialTabs(gui);
	}

	protected void renderSpecialTabs(GuiUtils gui) {
		for (int i = 0; i < SPECIAL_TABS.size(); i++) {
			int tabXSpecial = parentScreen.getLeftPos() + parentScreen.imageLength - 38 - i * 32;
			String selectedWord = (this.isSelected(true, i) ? "_selected" : "");
			gui.blitMonoImage(TAB_LOCATION.apply("below_middle" + selectedWord), tabXSpecial, this.getTabY(-1), 28, 32);
			this.renderItemInTab(gui, null, i, -1);
		}
	}

	protected void renderContentTabs(GuiUtils gui) {
		int count = 0;
		for (int i = tabPage * 10; i < tabPage * 10 + 10; i++) {
			if (i < CONTENT_TABS.size()) {
				boolean isRight = count >= 5;
				String selectedWord = (this.isSelected(false, i) ? "_selected" : "");
				int tabXSpecial = parentScreen.getLeftPos() + (isRight ? parentScreen.imageLength - 4 : -28);
				gui.blitMonoImage(TAB_LOCATION.apply((isRight ? "right" : "left") + "_middle" + selectedWord), tabXSpecial, this.getTabY(count), 32, 28);
				this.renderItemInTab(gui, isRight, i, count);
				count++;
			} else break;
		}
	}

	protected void renderItemInTab(GuiUtils gui, @Nullable Boolean isRight, int listIndex, int offsetIndex) {
		int tabX;
		boolean isDown = isRight == null;
		if (isDown) {
			tabX = parentScreen.getLeftPos() + parentScreen.imageLength - 38 - listIndex * 32 + 5;
		} else {
			tabX = parentScreen.getLeftPos() + (isRight ? parentScreen.imageLength + 2 : -19);
		}
		int tabY = this.getTabY(offsetIndex) + (isDown ? 7 : 5);

		GuiGraphics guiGraphics = gui.getGuiGraphics();
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.5f, 0f, 100f);
		ItemStack itemstack = (isDown ? SPECIAL_TABS : CONTENT_TABS).get(listIndex).getIconItem();
		guiGraphics.renderItem(itemstack, tabX, tabY);
		guiGraphics.renderItemDecorations(parentScreen.getFont(), itemstack, tabX, tabY);
		guiGraphics.pose().popPose();
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
		return BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(name);
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
