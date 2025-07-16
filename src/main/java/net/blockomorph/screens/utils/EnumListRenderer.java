package net.blockomorph.screens.utils;

import net.minecraft.client.gui.Font;

import java.util.List;
import java.util.function.Consumer;

public class EnumListRenderer {
	private final int textColor, hoveredTextColor, maxWordsOnList;
	private int x, y;
	private int startOfX, startOfY;
	private List<Enum<?>> enumList;
	private Consumer<Enum<?>> onClick;
	private Font font;
	private boolean dropped;

	public EnumListRenderer(int textColor, int hoveredTextColor, int maxWordsOnList) {
		this.textColor = textColor;
		this.hoveredTextColor = hoveredTextColor;
		this.maxWordsOnList = maxWordsOnList;
	}

	public void render(GuiUtils gui, int x, int y, int startOfX, int startOfY) {
		this.x = x;
		this.y = y;
		this.startOfX = startOfX;
		this.startOfY = startOfY;
		this.font = gui.getFont();
		if (this.dropped && this.enumList != null && this.onClick != null) {
			int startX = this.x + this.startOfX;
			int startY = this.y + this.startOfY;
			int longestWord = this.getLongestWord();
			int heightWord = 12 * Math.min(this.enumList.size(), 7);
			g
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY) {

	}

	public void drop(List<Enum<?>> list, Consumer<Enum<?>> onClick) {
		this.enumList = list;
		this.onClick = onClick;
		this.dropped = true;
	}

	public void close() {
		this.dropped = false;
	}

	private int getLongestWord() {
		int maxWidth = 0;
		for (Enum<?> enumValue : this.enumList) {
			String string = enumValue.toString().toLowerCase();
			int stringWidth = this.font.width(string);
			if (stringWidth > maxWidth) {
				maxWidth = stringWidth;
			}
		}
		return maxWidth;
	}
}
