package net.blockomorph.core;

import net.blockomorph.screens.TestScreen;
import net.blockomorph.screens.TestScreen2;
import net.blockomorph.screens.config.ConfigScreen;
import net.blockomorph.screens.config.ConfigScreenOld;
import net.blockomorph.screens.morph.AbstractMorphScreen;
import net.blockomorph.screens.morph.MorphScreen;
import net.blockomorph.screens.morph.MorphScreenOld;
import net.blockomorph.screens.morphConfig.BlockMorphConfigScreenOld;
import net.blockomorph.screens.morphConfig.MorphConfigScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;


public class KeyMappings {
    private static final Minecraft mc = Minecraft.getInstance();
	private static final ArrayList<KeyMapping> KEYS = new ArrayList<>();

	public static final KeyMapping MORPH = new HandlerKeymapping("key.blockomorph.morph_menu", GLFW.GLFW_KEY_Y, () ->
			mc.setScreen(new MorphScreen().ignoreInitInput())
	);

	public static final KeyMapping MORPH_CONFIG = new HandlerKeymapping("key.blockomorph.morph_config_menu", GLFW.GLFW_KEY_U, () ->
			mc.setScreen(new MorphConfigScreen(true))
	);

	public static final KeyMapping CONFIG = new HandlerKeymapping("key.blockomorph.config_menu", GLFW.GLFW_KEY_N, () -> {
		if (canOpenConfig()) {
			mc.setScreen(new ConfigScreen());
		}
	});

	public static final KeyMapping DEBUG = new HandlerKeymapping("key.blockomorph.debug", GLFW.GLFW_KEY_J, () -> {
		if (true) {
			AbstractMorphScreen sc = new AbstractMorphScreen(AbstractMorphScreen.MorphScreenOptions.CONFIG) {
				@Override
				protected void initAdditional(Consumer<AbstractWidget> action) {

				}

				@Override
				protected void renderFrame(SavedBlock block, int x, int y) {

				}

				@Override
				protected SoundInstance onClickOnBlock(SavedBlock block, int number, CreativeModeTab selectedTab, int page) {
					GuiUtils.MC.setScreen(new TestScreen(block.getState(), true) {
						@Override
						protected void init() {
							super.init();
							Button button = Button.builder(Component.literal("<--"), b -> {
								open();
							}).pos(this.leftPos - 25, this.topPos).size(20, 20).build();
							this.addRenderableWidget(button);
						}
					});
					return null;
				}

				public void open() {
					GuiUtils.MC.setScreen(this);
				}
			};
			GuiUtils.MC.setScreen(sc);
		} else {
			GuiUtils.MC.setScreen(new TestScreen());
		}
	});

	public static final KeyMapping DEBUG2 = new HandlerKeymapping("key.blockomorph.debug2", GLFW.GLFW_KEY_K, () -> {
		mc.setScreen(new TestScreen2());
	});

	public static final KeyMapping DEBUG3 = new HandlerKeymapping("key.blockomorph.debug3", GLFW.GLFW_KEY_L, () -> {

	});

	static void registerKeyMappings(Consumer<KeyMapping> register) {
		for (KeyMapping key : KEYS) {
			register.accept(key);
		}
	}

	private static boolean canOpenConfig() {
		return mc.player != null && mc.player.hasPermissions(2) && Config.getInstance().getValue("canOperatorModifyConfig", Boolean.class);
	}

	private static class HandlerKeymapping extends KeyMapping {
		private final Runnable action;

		public HandlerKeymapping(String lang, int key, Runnable action) {
			super(lang, key, "key.categories.ui");
			KEYS.add(this);
			this.action = action;
		}

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDown && mc.screen == null) {
				this.action.run();
			}
		}
	}

}
