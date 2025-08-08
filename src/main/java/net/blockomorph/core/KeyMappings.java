package net.blockomorph.core;

import net.blockomorph.screens.TestScreen;
import net.blockomorph.screens.TestScreen2;
import net.blockomorph.screens.TestScreen3;
import net.blockomorph.screens.config.ConfigScreen;
import net.blockomorph.screens.morph.AbstractMorphScreen;
import net.blockomorph.screens.morph.MorphScreen;
import net.blockomorph.screens.morphConfig.MorphConfigScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


public class KeyMappings {
    private static final Minecraft mc = Minecraft.getInstance();
	private static final ArrayList<KeyMapping> KEYS = new ArrayList<>();

	public static final KeyMapping MORPH = new HandlerKeymapping("morph_menu", GLFW.GLFW_KEY_Y, () -> {
		Config.ScreenAccess access = MorphUtils.getScreenAccess(mc.player);
		if (access.morph) {
			mc.setScreen(new MorphScreen(new AbstractMorphScreen.MorphScreenOptions(true, true, access.config)).ignoreInitInput());
			return true;
		}
		return false;
	});

	public static final KeyMapping MORPH_CONFIG = new HandlerKeymapping("morph_config_menu", GLFW.GLFW_KEY_U, () -> {
		Config.ScreenAccess access = MorphUtils.getScreenAccess(mc.player);
		if (access.config) {
			mc.setScreen(new MorphConfigScreen(access.morph));
			return true;
		}
		return false;
	});

	public static final KeyMapping CONFIG = new HandlerKeymapping("config_menu", GLFW.GLFW_KEY_N, () -> {
		if (MorphUtils.canOpenConfig()) {
			mc.setScreen(new ConfigScreen());
			return true;
		}
		return false;
	});

	public static final KeyMapping DEBUG = new HandlerKeymapping("debug", GLFW.GLFW_KEY_J, () -> {
		if (false) {
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
			GuiUtils.MC.setScreen(new TestScreen(false));
		}
		return true;
	});

	public static final KeyMapping DEBUG2 = new HandlerKeymapping("debug2", GLFW.GLFW_KEY_K, () -> {
		mc.setScreen(new TestScreen2());
		return true;
	});

	public static final KeyMapping DEBUG3 = new HandlerKeymapping("debug3", GLFW.GLFW_KEY_L, () -> {
		mc.setScreen(new TestScreen3());
		return true;
	});

	static void registerKeyMappings(Consumer<KeyMapping> register) {
		for (KeyMapping key : KEYS) {
			register.accept(key);
		}
	}

	private static class HandlerKeymapping extends KeyMapping {
		private final BooleanSupplier action;

		public HandlerKeymapping(String lang, int key, BooleanSupplier action) {
			super("blockomorph.key." + lang, key, CATEGORY_INTERFACE);
			KEYS.add(this);
			this.action = action;
		}

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDown && mc.screen == null) {
				if (!this.action.getAsBoolean() && mc.level != null) {
					GuiUtils.pushHotbarMessage(Component.translatable(this.getName() + ".error").withStyle(ChatFormatting.RED));
				}
			}
		}
	}

}
