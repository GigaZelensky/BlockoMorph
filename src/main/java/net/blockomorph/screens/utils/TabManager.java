package net.blockomorph.screens.utils;

import net.blockomorph.screens.MorphScreen2;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderType;
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

public class TabManager {
	private static final CreativeModeTab ALLOWED_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).title(Component.translatable("gui.blockomorph.allowedBlocks")).icon(() -> new ItemStack(Items.NETHER_STAR)).build();
	private static final ResourceLocation SEARCH_BAR = GuiUtils.res("textures/screens/searchbar.png");
	private final MorphScreen2 parentScreen;
	private final List<CreativeModeTab> TABS = CreativeModeTabs.tabs();
	private final List<CreativeModeTab> CONTENT_TABS = BuiltInRegistries.CREATIVE_MODE_TAB.stream().filter((tab) -> {
		return tab.shouldDisplay() && tab.getType() == CreativeModeTab.Type.CATEGORY && tab != this.getTabFromKey(CreativeModeTabs.OP_BLOCKS);
	}).toList();
	private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
	private static int tabPage = 0;
	private final boolean useAllowedTab;
	private final boolean useSavedBlocksTab;

	public TabManager(MorphScreen2 screen, boolean useAllowedTab, boolean useSavedBlocksTab) {
		this.parentScreen = screen;
		this.useAllowedTab = useAllowedTab;
		this.useSavedBlocksTab = useSavedBlocksTab;
	}

	public void renderTabs(GuiUtils GUI, int mouseX, int mouseY) {
		if (this.hasSearchBar()) {
			GUI.blitMonoImage(SEARCH_BAR, parentScreen.getLeftPos() + 90, parentScreen.getTopPos() - 19, 80, 23);
		}
		//TABS
		CreativeModeTab tab = this.getTabAtPosition(mouseX, mouseY);
		if (tab != null) GUI.renderTooltip(tab.getDisplayName(), mouseX, mouseY);
	}

	public boolean hasSearchBar() {
		return selectedTab == CreativeModeTabs.searchTab();
	}

	public <T extends GuiEventListener & Renderable & NarratableEntry> void init(Consumer<T> action) {

	}

	protected void renderTabButton(GuiGraphics gui, CreativeModeTab tab, int i, boolean isLeft) {
		boolean isSelectedTab = tab == selectedTab;
		String tabType;
		int l = leftPos;
		int i1 = this.getTabY(i);
		int weight = 32;
		int height = 28;

		if (isLeft) {
			l -= 28;
			tabType = "left";
		} else {
			l += imageWidth - 4;
			tabType = "right";
		}
		if (i < 0) {
			tabType = "below";
			l += imageWidth - 10;
			if (i == -2) l -= 32;
			if (i == -3) l -= 96;
			if (i == -4) l -= 64;
			weight = 28;
			height = 32;
		}
		tabType = tabType + "_middle";
		if (flag) tabType = tabType + "_selected";

		gui.blit(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("textures/gui/sprites/advancements/tab_" + tabType + ".png"), l, i1, 0, 0, weight, height, weight, height);

		gui.pose().pushPose();
		gui.pose().translate(0.0F, 0.0F, 100.0F);

		ItemStack itemstack = tab.getIconItem();
		gui.renderItem(itemstack, l + 7, i1 + 5);
		gui.renderItemDecorations(this.font, itemstack, l + 7, i1 + 4);
		gui.pose().popPose();
	}

	private CreativeModeTab getTabAtPosition(double x, double y) {
		int leftPos = parentScreen.getLeftPos();
		int imageWidth = parentScreen.imageLength;

		int tabYSpecial = this.getTabY(-1);
		for (int i = 0; i < 4; i++) {
			int tabXSpecial = leftPos + imageWidth - 38 - i * 32;
			if (x > tabXSpecial && x < tabXSpecial + 28 && y > tabYSpecial && y < tabYSpecial + 32) {
				return switch (i) {
					case 0: CreativeModeTabs.searchTab();
					case 1: this.getTabFromKey(CreativeModeTabs.OP_BLOCKS);
					case 2: if (this.useSavedBlocksTab) this.getTabFromKey(CreativeModeTabs.HOTBAR);
					case 3: if (this.useAllowedTab) yield ALLOWED_TAB;
					default: yield null;
				};
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

	@Nullable
	public CreativeModeTab getTabFromKey(ResourceKey<CreativeModeTab> name) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getValue(name);
	}

	private int getTabY(int i) {
		if (i < 0) return parentScreen.getTopPos() + parentScreen.imageHeight - 4; //Special Tabs

		if (i > 4) i -= 5; //right column

		return parentScreen.getTopPos() + 3 + i * 32;
	}
}
