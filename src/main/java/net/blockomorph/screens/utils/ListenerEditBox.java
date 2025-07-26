package net.blockomorph.screens.utils;

import net.minecraft.client.gui.components.EditBox;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ListenerEditBox extends EditBox {
	private final ResourceLocation borderTexture;
	private final GuiUtils gui = new GuiUtils();
	private final Font font;
	private final Consumer<String> action;
	protected boolean editable = true;

	public ListenerEditBox(Font font, int x, int y, int length, int height, Component name, Consumer<String> action, @Nullable ResourceLocation border) {
		super(font, x, y, length, height, name);
		this.action = action;
		this.font = font;
		this.setBordered(false);
		this.borderTexture = border;
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		if (this.active && this.visible && this.editable) {
			boolean flag = super.keyPressed(key, scancode, mods);
			this.action.accept(this.getValue());
			return flag;
		}
		return false;
	}

	@Override
	public boolean charTyped(char character, int mods) {
		if (this.active && this.visible && this.editable) {
			boolean flag = super.charTyped(character, mods);
			this.action.accept(this.getValue());
			return flag;
		}
		return false;
	}

	@Override
	public void setEditable(boolean yes) {
		super.setEditable(yes);
		this.editable = yes;
	}

	@Override
	public void setBordered(boolean ignored) {
		super.setBordered(false);
	}

	@Override
	public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float ticks) {
		if (this.borderTexture != null) {
			this.gui.setGuiGraphics(g, this.font, mouseX, mouseY, ticks);
			this.gui.blit(this.borderTexture, this.getX(), this.getY(), 0, this.editable ? 0 : this.getHeight(), this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight() * 2);
		}
		super.renderWidget(g, mouseX, mouseY, ticks);
	}

}
