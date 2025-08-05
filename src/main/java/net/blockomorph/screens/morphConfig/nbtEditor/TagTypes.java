package net.blockomorph.screens.morphConfig.nbtEditor;

import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CollectionTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CompoundTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.primitive.NumericTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.primitive.StringTagRenderer;
import net.blockomorph.utils.MorphUtils;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class TagTypes {
	private static final HashMap<TagType<?>, TagRendererFactory<?>> RENDERERS = new HashMap<>();
	private static final HashMap<TagType<?>, TagCreator<?>> TAGS = new HashMap<>();

	public static <T extends Tag> void registerRenderer(TagType<T> type, TagRendererFactory<T> factory) {
		if (RENDERERS.containsKey(type)) throw new IllegalArgumentException("Type " + type.getName() + " already registered!");
		RENDERERS.put(type, factory);
	}

	public static void registerTagCreator(TagType<?> type, TagCreator<?> creator) {
		if (TAGS.containsKey(type)) throw new IllegalArgumentException("Type " + type.getName() + " already registered!");
		TAGS.put(type, creator);
	}

	public static TagType<?>[] getRegisteredTags() {
		TagType<?>[] array = new TagType<?>[TAGS.size()];
		int i = 0;
		for (TagType<?> tagType : TAGS.keySet()) {
			array[i] = tagType;
			i++;
		}
		return array;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T extends Tag> TagRenderer<T> getRendererForTag(String name, T tag, TagRendererContext<T> ctx) {
		try {
			TagRendererFactory<T> renderSource = (TagRendererFactory<T>) RENDERERS.get(tag.getType());
			if (renderSource != null) {
				return renderSource.create(name, tag, ctx);
			}
		} catch (ClassCastException e) {
			MorphUtils.LOGGER.error("Invalid tag registration for NBT Editor: ", e);
			return null;
		}
		return null;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T extends Tag> TagCreator<T> createTag(TagType<T> type) {
		try {
			return (TagCreator<T>) TAGS.get(type);
		} catch (ClassCastException e) {
			MorphUtils.LOGGER.error("Invalid tag registration for NBT Editor: ", e);
			return null;
		}
	}

	@FunctionalInterface
	public interface TagRendererFactory<T extends Tag> {
		TagRenderer<T> create(String tagName, T tag, TagRendererContext<T> ctx);
	}

	public interface TagCreator<T extends Tag> {
		@Nullable T create(String value);

		default boolean noHasValue() {
			return false;
		}

		T defaultValue();
	}

	static {
		registerRenderer(CompoundTag.TYPE, CompoundTagRenderer::new);
		registerRenderer(StringTag.TYPE, StringTagRenderer::new);

		registerRenderer(IntTag.TYPE, NumericTagRenderer.IntTagRenderer::new);
		registerRenderer(LongTag.TYPE, NumericTagRenderer.LongTagRenderer::new);
		registerRenderer(DoubleTag.TYPE, NumericTagRenderer.DoubleTagRenderer::new);
		registerRenderer(FloatTag.TYPE, NumericTagRenderer.FloatTagRenderer::new);
		registerRenderer(ShortTag.TYPE, NumericTagRenderer.ShortTagRenderer::new);
		registerRenderer(ByteTag.TYPE, NumericTagRenderer.ByteTagRenderer::new);

		registerRenderer(ListTag.TYPE, CollectionTagRenderer.ListTagRenderer::new);
		registerRenderer(IntArrayTag.TYPE, CollectionTagRenderer.IntArrayTagRenderer::new);
		registerRenderer(LongArrayTag.TYPE, CollectionTagRenderer.LongArrayTagRenderer::new);
		registerRenderer(ByteArrayTag.TYPE, CollectionTagRenderer.ByteArrayTagRenderer::new);
	}

	private static class StringTagCreator implements TagCreator<StringTag> {
		@Override
		public StringTag create(String value) {
			return StringTag.valueOf(value);
		}

		@Override
		public StringTag defaultValue() {
			return StringTag.valueOf("");
		}
	}

	private static class CompoundTagCreator implements TagCreator<CompoundTag> {
		@Override
		public CompoundTag create(String value) {
			return new CompoundTag();
		}

		@Override
		public boolean noHasValue() {
			return true;
		}

		@Override
		public CompoundTag defaultValue() {
			return new CompoundTag();
		}
	}

	private record NumberArrayTagCreator<T extends CollectionTag>(Supplier<T> creator) implements TagCreator<T> {
		@Override
		public T create(String value) {
			return this.creator.get();
		}

		@Override
		public T defaultValue() {
			return this.creator.get();
		}

		@Override
		public boolean noHasValue() {
			return true;
		}
	}

	private static class ListTagCreator implements TagCreator<ListTag> {
		@Override
		public ListTag create(String value) {
			return new ListTag();
		}

		@Override
		public boolean noHasValue() {
			return true;
		}

		@Override
		public ListTag defaultValue() {
			return new ListTag();
		}
	}

	private record NumericTagCreator<T extends NumericTag>(Function<String, T> creator, T defaultValue) implements TagCreator<T> {
		@Override
		public T create(String value) {
			try {
				return this.creator.apply(value);
			} catch (Exception ignored) {
				return null;
			}
		}

		@Override
		public T defaultValue() {
			return this.defaultValue;
		}
	}

	static {
		registerTagCreator(StringTag.TYPE, new StringTagCreator());
		registerTagCreator(CompoundTag.TYPE, new CompoundTagCreator());

		registerTagCreator(ListTag.TYPE, new ListTagCreator());
		registerTagCreator(IntArrayTag.TYPE, new NumberArrayTagCreator<>(() -> new IntArrayTag(new int[0])));
		registerTagCreator(LongArrayTag.TYPE, new NumberArrayTagCreator<>(() -> new LongArrayTag(new long[0])));
		registerTagCreator(ByteArrayTag.TYPE, new NumberArrayTagCreator<>(() -> new ByteArrayTag(new byte[0])));

		registerTagCreator(IntTag.TYPE, new NumericTagCreator<>(value -> IntTag.valueOf(Integer.parseInt(value)), IntTag.valueOf(0)));
		registerTagCreator(LongTag.TYPE, new NumericTagCreator<>(value -> LongTag.valueOf(Long.parseLong(value)), LongTag.valueOf(0)));
		registerTagCreator(DoubleTag.TYPE, new NumericTagCreator<>(value -> DoubleTag.valueOf(Double.parseDouble(value)), DoubleTag.valueOf(0)));
		registerTagCreator(FloatTag.TYPE, new NumericTagCreator<>(value -> FloatTag.valueOf(Float.parseFloat(value)), FloatTag.valueOf(0)));
		registerTagCreator(ShortTag.TYPE, new NumericTagCreator<>(value -> ShortTag.valueOf(Short.parseShort(value)), ShortTag.valueOf((short)0)));
		registerTagCreator(ByteTag.TYPE, new NumericTagCreator<>(value -> ByteTag.valueOf(Byte.parseByte(value)), ByteTag.valueOf((byte)0)));
	}
}
