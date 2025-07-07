package net.blockomorph.screens.utils;

import com.google.common.collect.ImmutableList;
import net.blockomorph.screens.MorphScreen2;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class BlocksManager {
	protected static final ResourceKey<CreativeModeTab> ALLOWED_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, GuiUtils.res("allowed_blocks"));
	protected final static HashMap<ResourceKey<CreativeModeTab>, List<SavedBlock>> ALL_TAB_CONTENTS = new HashMap<>();
	protected static List<Block> ALL_BLOCKS;
	private static FeatureFlagSet FEATURE_FLAGS;
	private static HolderLookup.Provider HOLDER;
	private final MorphScreen2 parentScreen;

	public BlocksManager(MorphScreen2 screen) {
		this.parentScreen = screen;
	}

	public void render(GuiUtils gui) {
		gui.renderBlockInGui(Blocks.STONE.defaultBlockState(), null, gui.getMouseX(), gui.getMouseY(), 10);
	}

	public List<CreativeModeTab> sortTabsIfItemsIsBlocks() {
		LocalPlayer player = (LocalPlayer)parentScreen.getPlayer();
		FeatureFlagSet set = player.connection.enabledFeatures();
		HolderLookup.Provider holder = player.level().registryAccess();
		if (!set.equals(FEATURE_FLAGS) || HOLDER != holder) {
			ALL_TAB_CONTENTS.clear();
			ALL_BLOCKS = BuiltInRegistries.BLOCK.stream().filter((block -> block.isEnabled(set))).toList();

			this.putContentTabs(set);
			this.putSearchTab();
			this.putUnsortableTab();
		}
		this.putSavedTab();
		this.putAllowedTab();
		FEATURE_FLAGS = set;
		HOLDER = holder;
		return BuiltInRegistries.CREATIVE_MODE_TAB.stream().map(tab -> {
			ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElseThrow();
			if (tab.getType() == CreativeModeTab.Type.CATEGORY && key != CreativeModeTabs.OP_BLOCKS) {
				List<SavedBlock> list = ALL_TAB_CONTENTS.get(key);
				if (list != null && !list.isEmpty()) {
					return tab;
				}
			}
			return null;
		}).filter(Objects::nonNull).toList();
	}

	protected void putContentTabs(FeatureFlagSet set) {
		BuiltInRegistries.CREATIVE_MODE_TAB.entrySet().forEach((entry) -> {
			ResourceKey<CreativeModeTab> key = entry.getKey();
			CreativeModeTab tab = entry.getValue();
			if (tab.getType() == CreativeModeTab.Type.CATEGORY) {
				List<SavedBlock> blocks = tab.getDisplayItems().stream().map((item) -> {
					if (item.getItem() instanceof BlockItem block && block.isEnabled(set)) {
						return new SavedBlock(this.prepareBlockStateTag(block.getBlock().defaultBlockState(), item), null, null);
					}
					return null;
				}).filter(Objects::nonNull).toList();
				ALL_TAB_CONTENTS.put(key, blocks);
			}
		});
	}

	private BlockState prepareBlockStateTag(BlockState blockState, ItemStack item) {
		BlockItemStateProperties properties = item.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		if (!properties.isEmpty()) {
			return properties.apply(blockState);
		}
		return blockState;
	}

	protected void putSearchTab() {
		ALL_TAB_CONTENTS.put(CreativeModeTabs.SEARCH, ALL_BLOCKS.stream().map((block -> new SavedBlock(block.defaultBlockState(), null, null))).toList());
	}

	protected void putUnsortableTab() {
		List<Block> newList = new ArrayList<>();
		ALL_BLOCKS.forEach((block -> {
			boolean contains = false;
			for (Map.Entry<ResourceKey<CreativeModeTab>, List<SavedBlock>> entry : ALL_TAB_CONTENTS.entrySet()) {
				if (entry.getKey() != CreativeModeTabs.SEARCH) {
					List<SavedBlock> list = entry.getValue();
					for (SavedBlock block2 : list) {
						if (block2.getState() != null && block2.getState().getBlock() == block) {
							contains = true;
							break;
						}
					}
				}
			}
			if (!contains && !newList.contains(block)) {
				newList.add(block);
			}
		}));
		List<SavedBlock> old = ALL_TAB_CONTENTS.get(CreativeModeTabs.OP_BLOCKS);
		ImmutableList.Builder<SavedBlock> builder = ImmutableList.builder();
		if (old != null) builder.addAll(old);
		builder.addAll(newList.stream().map((block -> new SavedBlock(block.defaultBlockState(), null, null))).toList());
		ALL_TAB_CONTENTS.put(CreativeModeTabs.OP_BLOCKS, builder.build());
	}

	protected void putSavedTab() {
		ALL_TAB_CONTENTS.put(CreativeModeTabs.HOTBAR, MorphScreen2.SAVED_BLOCK_MANAGER.get().values().stream().toList());
	}

	protected void putAllowedTab() {
		ALL_TAB_CONTENTS.put(ALLOWED_TAB_KEY, ALL_BLOCKS.stream().map((block -> {
			if (MorphUtils.isBannedBlock(block.defaultBlockState(), parentScreen.getPlayer().player()) != null) {
				return new SavedBlock(block.defaultBlockState(), null, null);
			}
			return null;
		})).filter(Objects::nonNull).toList());
	}
}
