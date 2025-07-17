package net.blockomorph.screens.config;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.morph.AbstractMorphScreen;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.config.BlockListConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Consumer;

public class ListOptionEditingMorphScreen extends AbstractMorphScreen {
	private final BlockListConfig blockListConfig;
	private final Screen parentScreen;

	public ListOptionEditingMorphScreen(BlockListConfig configInstance, Screen parentScreen) {
		super(MorphScreenOptions.CONFIG);
		this.blockListConfig = configInstance;
		this.parentScreen = parentScreen;
	}

	@Override
	protected void initAdditional(Consumer<AbstractWidget> action) {
		Button butt = Button.builder(Component.literal("<--"), b -> {
			mc.setScreen(this.parentScreen);
		}).pos(this.leftPos + 10, this.topPos + this.imageHeight + 1).size(20, 20).build();
		action.accept(butt);
	}

	@Override
	protected void renderFrame(SavedBlock block, int x, int y) {
		String name = BuiltInRegistries.BLOCK.getKey(block.getState().getBlock()).toString();
		boolean contains = this.blockListConfig.getValue().contains(name);
		if (contains)
			gui.blitMonoImage(this.blockListConfig.getFrameTexture(), x, y, 36, 36);
	}

	@Override
	protected SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page) {
		String name = BuiltInRegistries.BLOCK.getKey(block.getState().getBlock()).toString();
		boolean contains = this.blockListConfig.getValue().contains(name);
		MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(this.blockListConfig.getName(), (contains ? "- " : "+ ") + name));
		return SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f);
	}
}
