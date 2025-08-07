package net.blockomorph.screens;

import net.blockomorph.screens.utils.ListenerEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class TestScreen3 extends Screen {
	public TestScreen3() {
		super(CommonComponents.EMPTY);
	}

	@Override
	protected void init() {
		super.init();
		ListenerEditBox box = new ListenerEditBox(this.font, this.width/2, this.height/2, 100, 20, Component.literal("name"), v -> {
			System.out.println(v);
		}, ListenerEditBox.VANILLA);
		this.addRenderableWidget(box);
	}
}
