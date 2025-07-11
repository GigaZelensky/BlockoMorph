package net.blockomorph.screens.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class SpriteImageButton extends Button {
	private final GuiUtils utils = new GuiUtils();
	private final ResourceLocation sprite;
	private final Supplier<Boolean> activated;
	private final boolean useInActive;
	private final int spriteSize;
	public SpriteImageButton(int x, int y, int lengthButtonOnScreen, int heightButtonOnScreen, ResourceLocation sprite, OnPress onPress, @Nullable Supplier<Boolean> active, boolean useInActive) {
		super(x, y, lengthButtonOnScreen, heightButtonOnScreen, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
		this.sprite = sprite;
		this.activated = active;
		this.spriteSize = useInActive ? 3 : 2;
		this.useInActive = useInActive;
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		if (this.activated != null) {
			this.active = this.activated.get();
		}
		utils.setGuiGraphics(guiGraphics, Minecraft.getInstance().font, mouseX, mouseY, delta);
		int y = 0;
		if (!this.active) {
			if (this.useInActive) {
				y = this.getHeight() * 2;
			} else throw new IllegalArgumentException("Button disabled, but sprite not contains INACTIVE texture!");
		} else {
			if (this.isHovered) {
				y = this.getHeight();
			}
		}
		utils.blit(sprite, this.getX(), this.getY(), 0, y, this.getWidth(), this.getHeight(), this.getWidth(), this.getHeight() * this.spriteSize);
	}
}
