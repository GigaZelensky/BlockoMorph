package net.blockomorph.screens.config;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ConfigScreen extends Screen {
	private static final ResourceLocation MENU_LOCATION = GuiUtils.res("textures/screens/config_screen.png");
	protected final GuiUtils gui = new GuiUtils();
	private ConfigRenderableList configList;
	public final int imageLength = 176;
	public final int imageHeight = 166;
	protected int leftPos;
	protected int topPos;

	public ConfigScreen() {
		super(Component.literal("config_screen"));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, tick);
		super.render(guiGraphics, mouseX, mouseY, tick);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, tick);
		this.gui.blitMonoImage(MENU_LOCATION, this.leftPos, this.topPos, this.imageLength, this.imageHeight);
		this.gui.drawString(Component.translatable("menu.options"), this.leftPos + 8, this.topPos + 6, 4210752, false);
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageLength) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		float scrollOff = 0f;
		if (this.configList != null) {
			scrollOff = this.configList.scrollerManager.getScrollerOffset();
		}
		this.configList = new ConfigRenderableList(Config.getInstance().OPTIONS, this.leftPos + 10, this.topPos + 15, 144, 140, this.leftPos + 158, this.topPos + 16, 142, 20, 144, this);
		this.configList.scrollerManager.setScrollOffset(scrollOff);
		this.configList.scrollerManager.refreshList();
		this.addRenderableWidget(this.configList);
	}

	@Override
	public Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
		return Optional.of(this.configList);
	}

	@Override
	public GuiEventListener getFocused() {
		return this.configList;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
