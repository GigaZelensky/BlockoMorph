package net.blockomorph.screens.utils;

public class EnumListRenderer<T extends Enum<?>> extends ObjectListRenderer<T> {
	public EnumListRenderer(int textColor, int hoveredTextColor, int maxWordsOnList) {
		super(textColor, hoveredTextColor, maxWordsOnList, value -> value.toString().toLowerCase());
	}
}
