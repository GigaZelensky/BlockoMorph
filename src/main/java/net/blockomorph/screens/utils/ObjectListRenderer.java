package net.blockomorph.screens.utils;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ObjectListRenderer<T> {
	private final int textColor, hoveredTextColor, maxWordsOnList;
	private int x, y;
	private int startOfX, startOfY;
	protected List<T> list;
	protected Consumer<T> onClick;
	protected final Function<T, String> toString;
	private Font font;
	private boolean dropped;
	private int listOffset;

	public ObjectListRenderer(int textColor, int hoveredTextColor, int maxWordsOnList, Function<T, String> toString) {
		this.textColor = textColor;
		this.hoveredTextColor = hoveredTextColor;
		this.maxWordsOnList = maxWordsOnList;
		this.toString = toString;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public Class<T> getValuesClass() {
		if (this.list != null && !this.list.isEmpty()) {
			return (Class<T>) this.list.getFirst().getClass();
		}
		return null;
	}

	public void renderName(GuiUtils gui, int x, int y, int maxLength, T value, int stringColor) {
		String name = this.toString.apply(value);
		String newName = gui.getFont().plainSubstrByWidth(name, maxLength);
		if (!newName.equals(name)) {
			newName = newName + "..";
		}
		gui.drawString(Component.literal(newName), x, y, stringColor, false);
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
			int heightWords = 12 * Math.min(this.list.size(), this.maxWordsOnList);
			gui.fill(startX, startY, startX + longestWord + 4, startY + heightWords, Integer.MIN_VALUE);

			int textX = startX + 2;
			for (int i = 0; i < Math.min(this.maxWordsOnList, this.list.size()); i++) {
				int textY = startY + i*12 + 2;
				int color = GuiUtils.isMouseOver(textX, textY - 2, textX + longestWord, textY + 10, gui.getMouseX(), gui.getMouseY()) ? this.hoveredTextColor : this.textColor;
				gui.drawString(this.getName(this.list.get(Math.min(i + this.listOffset, this.list.size() - 1))), textX, textY, color, true);
			}

			if (this.list.size() > this.maxWordsOnList) {
				if (this.listOffset + this.maxWordsOnList < this.list.size()) {
					this.renderDots(gui, startX, startY + heightWords, longestWord + 4); //down dots
				}
			}

			if (this.listOffset > 0) {
				this.renderDots(gui, startX, startY, longestWord + 4); //up dots
			}
		}
	}

	private Component getName(T value) {
		return Component.literal(this.toString.apply(value));
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
			int endY = startY + 12 * Math.min(this.list.size(), this.maxWordsOnList);
			if (GuiUtils.isMouseOver(startX, startY, endX, endY, mouseX, mouseY)) {
				if (yScrolled > 0) {
					int result = this.listOffset - 1;
					if (result >= 0) {
						this.listOffset = result;
					}
				} else if (yScrolled < 0) {
					int result = this.listOffset + 1;
					if (result + this.maxWordsOnList <= this.list.size()) {
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
			for (int i = 0; i < Math.min(this.maxWordsOnList, this.list.size()); i++) {
				int textY = this.y + this.startOfY + i*12 + 2;
				if (GuiUtils.isMouseOver(textX, textY - 2, textXend, textY + 10, mouseX, mouseY)) {
					T value = this.list.get(Math.min(i + this.listOffset, this.list.size() - 1));
					this.onClick.accept(value);
					this.close();
					return true;
				}
			}
		}
		return false;
	}

	public void drop(List<T> list, Consumer<T> onClick) {
		List<?> old = this.list;
		this.list = Objects.requireNonNull(list);
		this.onClick = Objects.requireNonNull(onClick);
		this.dropped = true;
		if (!list.equals(old)) {
			this.listOffset = 0;
		}
	}

	public void close() {
		this.dropped = false;
		this.listOffset = 0;
		this.list = null;
		this.onClick = null;
	}

	private int getLongestWord() {
		int maxWidth = 0;
		for (T value : this.list) {
			String string = this.toString.apply(value);
			int stringWidth = this.font.width(string);
			if (stringWidth > maxWidth) {
				maxWidth = stringWidth;
			}
		}
		return maxWidth;
	}
}
