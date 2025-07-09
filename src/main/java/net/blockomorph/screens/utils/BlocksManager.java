package net.blockomorph.screens.utils;

import com.google.common.collect.ImmutableList;
import net.blockomorph.screens.MorphScreen2;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlocksManager {
	protected static final ResourceKey<CreativeModeTab> ALLOWED_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, GuiUtils.res("allowed_blocks"));
	protected final static HashMap<ResourceKey<CreativeModeTab>, List<SavedBlock>> ALL_TAB_CONTENTS = new HashMap<>();
	protected final List<SavedBlock> renderableBlocks = new ArrayList<>(16) {
		@Override
		public SavedBlock get(int index) {
			if (index >= size() || index < 0) {
				return null;
			}
			return super.get(index);
		}
	};
	protected static List<Block> ALL_BLOCKS;
	private static FeatureFlagSet FEATURE_FLAGS;
	private static HolderLookup.Provider HOLDER;
	public final ScrollerManager<SavedBlock> scrollerManager;
	private final MorphScreen2 parentScreen;
	private final boolean needAccessCheck;

	public BlocksManager(MorphScreen2 screen, boolean needAccessCheck) {
		this.parentScreen = screen;
		this.needAccessCheck = needAccessCheck;
		this.scrollerManager = new ScrollerManager<>(() -> screen.getLeftPos() + 158, () -> screen.getTopPos() + 16, 142, 4, 4, this.renderableBlocks);
	}

	public void render(GuiUtils gui) {
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				SavedBlock block = this.renderableBlocks.get(y * 4 + x);
				if (block != null) {
					BlockEntity blockEntity = (block.getState().getBlock() instanceof EntityBlock ent ? ent.newBlockEntity(GuiUtils.AIR, block.getState()) : null);
					if (blockEntity != null) {
						blockEntity.setLevel(MorphScreen2.mc.level);
						if (block.getTag() != null) {
							blockEntity.loadWithComponents(block.getTag(), parentScreen.getPlayer().player().registryAccess());
						}
					}
					gui.renderBlockInGui(block.getState(), blockEntity, parentScreen.getLeftPos() + 28 + x * 36, parentScreen.getTopPos() + 48.5f + y * 36, 20);
				}
			}
		}
		this.scrollerManager.renderScroller(gui);
		SavedBlock block = this.getBlockAtPosition(gui.getMouseX(), gui.getMouseY());
		if (block != null) {
			gui.renderTooltip(block.getState().getBlock().getName(), gui.getMouseX(), gui.getMouseY());
		}
	}

	@Nullable
	public SavedBlock getBlockAtPosition(double x, double y) {
		return this.renderableBlocks.get(this.findBlockIndex(x, y));
	}

	public int findBlockIndex(double x, double y) {
		int leftPos = parentScreen.getLeftPos() + 11;
		int topPos = parentScreen.getTopPos() + 16;

		if (x < leftPos || x >= leftPos + 4 * 35.5 || y < topPos || y >= topPos + 4 * 35.5) {
			return -1;
		}

		int col = (int) ((x - leftPos) / 35.5);
		int row = (int) ((y - topPos) / 35.5);

		return row * 4 + col;
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
			ResourceKey<CreativeModeTab> key = TabManager.getKeyFromTab(tab);
			if (tab.getType() == CreativeModeTab.Type.CATEGORY && key != CreativeModeTabs.OP_BLOCKS) {
				List<SavedBlock> list = ALL_TAB_CONTENTS.get(key);
				if (list != null && !list.isEmpty()) {
					return tab;
				}
			}
			return null;
		}).filter(Objects::nonNull).toList();
	}

	public boolean mouseClicked(double x, double y, MorphScreen2.OnBlockClick click) {
		SavedBlock block = parentScreen.BLOCKS_MANAGER.getBlockAtPosition(x, y);
		if (block != null) {
			SoundInstance sound = click.click(parentScreen.getPlayer().player().level(), parentScreen.getPlayer(), block, parentScreen.BLOCKS_MANAGER.findBlockIndex(x, y), TabManager.getSelectedTab(), TabManager.getTabPage());
			if (sound != null) {
				MorphScreen2.mc.getSoundManager().play(sound);
			}
			return true;
		} else return this.scrollerManager.mouseClicked(x, y);
	}

	protected void putContentTabs(FeatureFlagSet set) {
		BuiltInRegistries.CREATIVE_MODE_TAB.entrySet().forEach((entry) -> {
			ResourceKey<CreativeModeTab> key = entry.getKey();
			CreativeModeTab tab = entry.getValue();
			if (tab.getType() == CreativeModeTab.Type.CATEGORY) {
				List<SavedBlock> blocks = tab.getDisplayItems().stream().map((item) -> {
					if (item.getItem() instanceof BlockItem block && block.isEnabled(set) && this.isAllowed(block)) {
						return new SavedBlock(this.prepareBlockStateTag(block.getBlock().defaultBlockState(), item), null, null);
					}
					return null;
				}).filter(Objects::nonNull).toList();
				ALL_TAB_CONTENTS.put(key, blocks);
			}
		});
	}

	protected boolean isAllowed(BlockItem block) {
		boolean allow = MorphUtils.isBannedBlock(block.getBlock().defaultBlockState(), parentScreen.getPlayer().player()) == null;
		return !this.needAccessCheck || allow;
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
			if (MorphUtils.isBannedBlock(block.defaultBlockState(), parentScreen.getPlayer().player()) == null) {
				return new SavedBlock(block.defaultBlockState(), null, null);
			}
			return null;
		})).filter(Objects::nonNull).toList());
	}
}
