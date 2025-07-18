package net.blockomorph.screens.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class EnumListRenderer {
	private final int textColor, hoveredTextColor, maxWordsOnList;
	private int x, y;
	private int startOfX, startOfY;
	private List<? extends Enum<?>> enumList;
	private Consumer<Enum<?>> onClick;
	private Font font;
	private boolean dropped;
	private int listOffset;

	public EnumListRenderer(int textColor, int hoveredTextColor, int maxWordsOnList) {
		this.textColor = textColor;
		this.hoveredTextColor = hoveredTextColor;
		this.maxWordsOnList = maxWordsOnList;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public Class<? extends Enum<?>> getEnumClass() {
		if (this.enumList != null && !this.enumList.isEmpty()) {
			return (Class<? extends Enum<?>>) this.enumList.getFirst().getClass();
		}
		return null;
	}

	public boolean renderName(GuiUtils gui, int x, int y, int maxLength, Enum<?> enumValue, int stringColor) {
		boolean flag = false;
		String name = enumValue.toString().toLowerCase();
		String newName = gui.getFont().plainSubstrByWidth(name, maxLength);
		if (!newName.equals(name)) {
			newName = newName + "..";
			flag = true;
		}
		gui.drawString(Component.literal(newName), x, y, stringColor, false);
		return flag;
	}

	public void render(GuiUtils gui, int x, int y, int startOfX, int startOfY) {
		this.x = x;
		this.y = y;
		this.startOfX = startOfX;
		this.startOfY = startOfY;
		this.font = gui.getFont();

		if (this.dropped) {
			int startX = this.x + this.startOfX;
			int startY = this.y + this.startOfY;
			int longestWord = this.getLongestWord();
			int heightWords = 12 * Math.min(this.enumList.size(), this.maxWordsOnList);
			gui.fill(startX, startY, startX + longestWord + 4, startY + heightWords, Integer.MIN_VALUE);

			int textX = startX + 2;
			for (int i = 0; i < Math.min(this.maxWordsOnList, this.enumList.size()); i++) {
				int textY = startY + i*12 + 2;
				int color = GuiUtils.isMouseOver(textX, textY - 2, textX + longestWord, textY + 10, gui.getMouseX(), gui.getMouseY()) ? this.hoveredTextColor : this.textColor;
				gui.drawString(Component.literal(this.enumList.get(Math.min(i + this.listOffset, this.enumList.size() - 1)).toString().toLowerCase()), textX, textY, color, true);
			}

			if (this.enumList.size() > this.maxWordsOnList) {
				if (this.listOffset + this.maxWordsOnList < this.enumList.size()) {
					this.renderDots(gui, startX, startY + heightWords, longestWord + 4); //down dots
				}
			}

			if (this.listOffset > 0) {
				this.renderDots(gui, startX, startY, longestWord + 4); //up dots
			}
		}
	}

	private void renderDots(GuiUtils gui, int x, int y, int length) {
		for (int i = 0; i < length; i++) {
			if (i % 2 == 0) {
				int dotX = x + i;
				gui.fill(dotX, y, dotX + 1, y + 1, -1);
			}
		}
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double yScrolled) {
		if (this.dropped) {
			int startX = this.x + this.startOfX;
			int startY = this.y + this.startOfY;
			int endX = startX + this.getLongestWord();
			int endY = startY + 12 * Math.min(this.enumList.size(), this.maxWordsOnList);
			if (GuiUtils.isMouseOver(startX, startY, endX, endY, mouseX, mouseY)) {
				if (yScrolled > 0) {
					int result = this.listOffset - 1;
					if (result >= 0) {
						this.listOffset = result;
					}
				} else if (yScrolled < 0) {
					int result = this.listOffset + 1;
					if (result + this.maxWordsOnList <= this.enumList.size()) {
						this.listOffset = result;
					}
				}
				return yScrolled != 0;
			}
		}
		return false;
	}

	public boolean mouseClicked(double mouseX, double mouseY) {
		if (this.dropped) {
			int textX = this.x + this.startOfX + 2;
			int textXend = this.getLongestWord() + textX;
			for (int i = 0; i < Math.min(this.maxWordsOnList, this.enumList.size()); i++) {
				int textY = this.y + this.startOfY + i*12 + 2;
				if (GuiUtils.isMouseOver(textX, textY - 2, textXend, textY + 10, mouseX, mouseY)) {
					Enum<?> enumValue = this.enumList.get(Math.min(i + this.listOffset, this.enumList.size() - 1));
					this.onClick.accept(enumValue);
					this.close();
					return true;
				}
			}
		}
		return false;
	}

	public void drop(List<? extends Enum<?>> list, Consumer<Enum<?>> onClick) {
		List<?> old = this.enumList;
		this.enumList = Objects.requireNonNull(list);
		this.onClick = Objects.requireNonNull(onClick);
		this.dropped = true;
		if (!list.equals(old)) {
			this.listOffset = 0;
		}
	}

	public void close() {
		this.dropped = false;
		this.listOffset = 0;
		this.enumList = null;
		this.onClick = null;
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
