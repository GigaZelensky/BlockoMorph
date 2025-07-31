package net.blockomorph.screens;

import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class TestScreen extends NbtEditorScreen {
	private static final CompoundTag tag = new CompoundTag();

	public TestScreen() {
		super(tag, () -> System.out.println(tag));
		tag.putString("opa", "ttsetys");
		for (int i = 0; i < 14; i++) {
			tag.putString(i + "", "value" + i);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.gui.drawString(Component.literal(tag.toString()), this.width / 2, this.topPos - 20, -1, false);
	}
}
