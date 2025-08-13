package net.blockomorph.screens.config;

import net.blockomorph.network.ServerBoundConfigUpdatePacket;
import net.blockomorph.screens.AbstractScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ConfigScreen extends AbstractScreen {
	private ConfigRenderableList configList;
	private final ConfigRenderer.ConfigRenderingContext context = new ConfigRenderer.ConfigRenderingContext(this, (name, value) -> {
		MorphUtils.sendServer(new ServerBoundConfigUpdatePacket(name, value));
	}, GuiUtils.MC::setScreen);

	public ConfigScreen() {
		super("config_screen", null);
	}

	@Override
	public void renderMenu() {
		super.renderMenu();
		this.gui.drawString(Component.translatable("menu.options"), this.leftPos + 8, this.topPos + 6, 4210752, false);
	}

	@Override
	protected void init() {
		super.init();
		float scrollOff = 0f;
		if (this.configList != null) {
			scrollOff = this.configList.scrollerManager.getScrollerOffset();
		}
		this.configList = new ConfigRenderableList(Config.getInstance().OPTIONS, this.context, this.leftPos + 10, this.topPos + 15, 144, 140, this.leftPos + 158, this.topPos + 16, 142, 20, 144);
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
}
