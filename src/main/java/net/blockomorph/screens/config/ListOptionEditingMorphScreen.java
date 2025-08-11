package net.blockomorph.screens.config;

import net.blockomorph.screens.morph.AbstractMorphScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.config.BlockListConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Consumer;

public class ListOptionEditingMorphScreen extends AbstractMorphScreen {
	private final BlockListConfig blockListConfig;
	private final ConfigRenderer.ConfigRenderingContext context;

	public ListOptionEditingMorphScreen(BlockListConfig configInstance, ConfigRenderer.ConfigRenderingContext context) {
		super(MorphScreenOptions.CONFIG);
		this.blockListConfig = configInstance;
		this.context = context;
	}

	@Override
	protected void initAdditional(Consumer<AbstractWidget> action) {
		Button butt = Button.builder(Component.literal("<--"), b -> {
			this.context.onNewScreenRequested().accept(this.context.parentScreen());
		}).pos(this.leftPos + 10, this.topPos + this.imageHeight + 1).size(20, 20).build();
		action.accept(butt);
	}

	@Override
	protected void renderFrame(SavedBlock block, int x, int y) {
		String name = BuiltInRegistries.BLOCK.getKey(block.getState().getBlock()).toString();
		boolean contains = this.blockListConfig.getValue().contains(name);
		if (contains)
			this.gui.blitMonoImage(this.blockListConfig.getFrameTexture(), x, y, BLOCK_FRAME_SIZE, BLOCK_FRAME_SIZE);
	}

	@Override
	protected SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page) {
		String name = BuiltInRegistries.BLOCK.getKey(block.getState().getBlock()).toString();
		boolean contains = this.blockListConfig.getValue().contains(name);
		this.context.onValueChanged().accept(this.blockListConfig.getName(), (contains ? "- " : "+ ") + name);
		return GuiUtils.getClickSound();
	}
}
