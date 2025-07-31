package net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ListenerEditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class StringTagRenderer extends TagRenderer<StringTag> {
	private static final BiFunction<String, Integer, FormattedCharSequence> SHADOW_DISABLE = (value, cursorPos) -> {
		return FormattedCharSequence.forward(value, Style.EMPTY.withShadowColor(0));
	};
	private final ListenerEditBox valueBox;
	public StringTagRenderer(String tagName, StringTag tag, Consumer<StringTag> onTagUpdate, Consumer<String> onEntering, Runnable onMainTagEdited) {
		super(tagName, tag, onTagUpdate, onEntering, onMainTagEdited);
		this.valueBox = new ListenerEditBox(GuiUtils.MC.font, 0, 0, 79, 11, Component.literal("String tag value"), value -> {
			this.updateThis(StringTag.valueOf(value));
		}, null);
		this.valueBox.setValue(this.getTag().value());
		this.valueBox.setMaxLength(8166);
		this.valueBox.setTextColor(ARGB.color(43, 55, 224));
		this.valueBox.setFormatter(SHADOW_DISABLE);
	}

	@Override
	public void render(GuiUtils gui) {
		this.renderPlate(gui, 0);
		this.valueBox.setPosition(this.box.getX() + this.box.getWidth() - 81, this.box.getY() + 6);
		this.valueBox.render(gui.getGuiGraphics(), gui.getMouseX(), gui.getMouseY(), gui.getTick());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY) {
		boolean result = this.valueBox.mouseClicked(mouseX, mouseY, 0);
		this.valueBox.setFocused(result);
		return false;
	}

	@Override
	public boolean charTyped(char character, int mods) {
		return this.valueBox.charTyped(character, mods);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		return this.valueBox.keyPressed(key, scancode, mods);
	}
}
