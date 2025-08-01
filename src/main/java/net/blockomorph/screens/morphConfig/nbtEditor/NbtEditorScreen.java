package net.blockomorph.screens.morphConfig.nbtEditor;

import net.blockomorph.screens.AbstractScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays.TagEditingOverlay;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CollectionTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CompoundTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.primitive.NumericTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.primitive.StringTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ListenerEditBox;
import net.blockomorph.screens.utils.ScrollerManager;
import net.blockomorph.utils.MorphUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtEditorScreen extends AbstractScreen {
	private static final HashMap<TagType<?>, TagRendererFactory<?>> RENDERERS = new HashMap<>();
	private static final ScrollerManager.CustomBarData SCOLLER = new ScrollerManager.CustomBarData(GuiUtils.res("textures/screens/nbt_scroller.png"), 7, 15);
	private final Consumer<String> onEntering = tagName -> {
		this.enterInTag(this.path + "/" + tagName);
	};
	private final CompoundTag editingTag;
	private final Consumer<CompoundTag> onEdited;
	private final Runnable onTagEdited;
	private EditBox tagBox;
	private Button pathExit;
	private int internalBoxX;
	private int internalBoxY;
	protected final int internalBoxLength = 173;
	protected final int internalBoxHeigth = 159;
	private final List<RenderableTag<?>> renderables;
	private final ScrollerManager<RenderableTag<?>> scrollerManager;
	private String path = "";
	private int frameColor = -1;
	private TagEditingOverlay overlay;
	private final Consumer<TagEditingOverlay> onTagReceived;

	public NbtEditorScreen(CompoundTag editingTag, Consumer<CompoundTag> onEdited) {
		super("nbt_editor_screen", new ScreenPosition(229, 191));
		this.editingTag = Objects.requireNonNull(editingTag).copy();
		this.onEdited = Objects.requireNonNull(onEdited);
		this.renderables = new ArrayList<>(7);
		this.scrollerManager = new ScrollerManager<>(() -> this.leftPos + 181, () -> this.topPos + 41, 138, 1, 7, this.renderables, SCOLLER);
		this.onTagEdited = () -> this.onEdited.accept(this.editingTag);
		this.onTagReceived = tagEditor -> {
			this.removeWidget(this.overlay);
			if (tagEditor != null) {
				this.overlay = tagEditor;
				tagEditor.init();
				this.addWidget(this.overlay);
			}
		};
	}

	static {
		register(CompoundTag.TYPE, CompoundTagRenderer::new);
		register(StringTag.TYPE, StringTagRenderer::new);

		register(IntTag.TYPE, NumericTagRenderer.IntTagRenderer::new);
		register(LongTag.TYPE, NumericTagRenderer.LongTagRenderer::new);
		register(DoubleTag.TYPE, NumericTagRenderer.DoubleTagRenderer::new);
		register(FloatTag.TYPE, NumericTagRenderer.FloatTagRenderer::new);
		register(ShortTag.TYPE, NumericTagRenderer.ShortTagRenderer::new);
		register(ByteTag.TYPE, NumericTagRenderer.ByteTagRenderer::new);

		register(ListTag.TYPE, CollectionTagRenderer.ListTagRenderer::new);
		register(IntArrayTag.TYPE, CollectionTagRenderer.IntArrayTagRenderer::new);
		register(LongArrayTag.TYPE, CollectionTagRenderer.LongArrayTagRenderer::new);
		register(ByteArrayTag.TYPE, CollectionTagRenderer.ByteArrayTagRenderer::new);
	}

	public static <T extends Tag> void register(TagType<T> type, TagRendererFactory<T> factory) {
		if (RENDERERS.containsKey(type)) throw new IllegalArgumentException("Type " + type.getName() + " already registered!");
		RENDERERS.put(type, factory);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		int i = 0;
		Component tooltip = null;
		for (RenderableTag<?> tag : this.renderables) {
			tag.render(this.gui, i * TagRenderer.PLATE_HEIGTH);
			i++;
			if (tooltip == null) {
				tooltip = tag.getTooltip(this.gui);
			}
		}
		if (this.overlay != null) {
			this.gui.fill(0, 0, this.width, this.height, ARGB.color(196, 84, 84, 84));
		} else if (tooltip != null) {
			this.gui.renderTooltip(tooltip, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (this.overlay != null) {
			return super.mouseClicked(mouseX, mouseY, type);
		} else if (type == 0) {
			if (this.forEachTag(renderer -> renderer.mouseClicked(mouseX, mouseY))) {
				this.tagBox.setFocused(false);
				return true;
			}
		}
		this.tagBox.setFocused(this.tagBox.isMouseOver(mouseX, mouseY));
		if (super.mouseClicked(mouseX, mouseY, type)) {
			return true;
		} else return this.scrollerManager.mouseClicked(mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int type, double mouseXOffset, double mouseYOffset) {
		if (this.overlay != null) {
			return super.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
		} else if (this.scrollerManager.mouseDragged(mouseY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		if (this.overlay != null) {
			return super.mouseScrolled(mouseX, mouseY, xWheelOffset, yWheelOffset);
		} else if (this.forEachTag(renderer -> renderer.mouseScrolled(mouseX, mouseY, yWheelOffset))) {
			return true;
		}
		return this.scrollerManager.mouseScrolled(yWheelOffset);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int type) {
		this.scrollerManager.disableScrollWork();
		return super.mouseReleased(mouseX, mouseY, type);
	}

	@Override
	public boolean charTyped(char character, int mods) {
		if (this.overlay != null) {
			return super.charTyped(character, mods);
		} else if (this.forEachTag(renderer -> renderer.charTyped(character, mods))) {
			return true;
		}
		return super.charTyped(character, mods);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		if (this.overlay != null) {
			return super.keyPressed(key, scancode, mods);
		} else if (this.forEachTag(renderer -> renderer.keyPressed(key, scancode, mods))) {
			return true;
		}
		return super.keyPressed(key, scancode, mods);
	}

	private boolean forEachTag(Function<RenderableTag<?>, Boolean> action) {
		for (RenderableTag<?> renderer : this.renderables) {
			if (action.apply(renderer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderMenu() {
		this.gui.fill(this.internalBoxX, this.internalBoxY, this.internalBoxX + this.internalBoxLength, this.internalBoxY + this.internalBoxHeigth, this.frameColor);
		super.renderMenu();
		this.scrollerManager.renderScroller(this.gui);
		this.gui.drawString(Component.literal(this.path), this.internalBoxX + 11, this.internalBoxY + 5, -1, false);
	}

	@Override
	protected void init() {
		super.init();
		this.internalBoxX = this.leftPos + 23;
		this.internalBoxY = this.topPos + 26;
		this.tagBox = new ListenerEditBox(this.font, this.leftPos + 23, this.topPos + 5, 129, 19, this.getTitle(), value -> {}, ListenerEditBox.EDITBOX_BORDER_SPRITE);
		this.addRenderableWidget(this.tagBox);
		this.pathExit = Button.builder(Component.literal("<--"), button -> {
			int slashIndex = this.path.lastIndexOf('/');
			if (slashIndex != -1) {
				String newPath = this.path.substring(0, slashIndex);
				this.enterInTag(newPath);
			}
		}).bounds(this.internalBoxX + 146, this.internalBoxY + 4, 22, 9).build();
		this.addRenderableWidget(this.pathExit);
		this.initList();
	}

	public void enterInTag(String path) {
		this.path = path;
		this.initList();
	}

	private void initList() {
		Tag root = this.editingTag;
		String previousName = "root";
		for (Iterator<String> it = Arrays.stream(this.path.split("/")).filter(v -> !v.isEmpty()).iterator(); it.hasNext();) {
			String tagName = it.next();
			TagRenderer<?> renderer = getRendererForTag(previousName, root, new TagRendererContext<>(this.onEntering, this.onTagEdited, this.onTagReceived));
			if (renderer != null) {
				Tag child = renderer.tryWalk(tagName);
				if (child != null) {
					root = child;
					previousName = tagName;
					continue;
				}
			}
			throw new IllegalArgumentException("Illegal path: " + this.path + " for tag: " + this.editingTag);
		}
		TagRenderer<?> rootRenderer = getRendererForTag("root", root, new TagRendererContext<>(this.onEntering, this.onTagEdited, this.onTagReceived));
		if (rootRenderer != null && rootRenderer.canEnterInTag()) {
			this.frameColor = root == this.editingTag ? ARGB.color(255, 84, 84, 84) : rootRenderer.getFrameColor();
			List<RenderableTag<?>> renderableTags = new ArrayList<>();
			for (TagRenderer<?> renderer : rootRenderer.getEnteringTags()) {
				renderableTags.add(new RenderableTag<>(renderer));
			}
			this.scrollerManager.setMainList(renderableTags.stream().filter(RenderableTag::isValid).toList());
			this.scrollerManager.setScrollOffset(0f);
			this.scrollerManager.refreshList();
			this.pathExit.visible = this.path.contains("/");
			return;
		}
		throw new IllegalArgumentException("Tag: " + root.getType().getName() + " no enterable!");
	}

	@Nullable @SuppressWarnings("unchecked")
	public static <T extends Tag> TagRenderer<T> getRendererForTag(String name, T tag, TagRendererContext<?> ctx) {
		try {
			TagRendererFactory<T> renderSource = (TagRendererFactory<T>) RENDERERS.get(tag.getType());
			if (renderSource != null) {
				return renderSource.create(name, tag, (TagRendererContext<T>) ctx);
			}
		} catch (ClassCastException e) {
			MorphUtils.LOGGER.error("Invalid tag registration for NBT Editor: ", e);
			return null;
		}
		return null;
	}

	private class RenderableTag<T extends Tag> {
		private TagRenderer<T> mainRenderer;

		private RenderableTag(TagRenderer<T> renderer) {
			this.mainRenderer = renderer;
			if (this.mainRenderer != null) {
				TagRenderer<T> additionalRenderer = renderer.getInterpretationRenderer(() -> {
					this.mainRenderer = renderer;
				});
				if (additionalRenderer != null) {
					this.mainRenderer = additionalRenderer;
				}
			}
		}

		public boolean isValid() {
			return this.mainRenderer != null;
		}

		public void render(GuiUtils gui, int yOffset) {
			this.mainRenderer.getBox().setPosition(NbtEditorScreen.this.internalBoxX + 10, NbtEditorScreen.this.internalBoxY + 14 + yOffset);
			this.mainRenderer.renderTag(gui);
		}

		@Nullable
		public Component getTooltip(GuiUtils gui) {
			return this.mainRenderer.getTooltip(gui);
		}

		public boolean mouseClicked(double mouseX, double mouseY) {
			return this.mainRenderer.mouseClicked(mouseX, mouseY);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double yOffsetWheel) {
			return this.mainRenderer.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}

		public boolean keyPressed(int key, int scancode, int mods) {
			return this.mainRenderer.keyPressed(key, scancode, mods);
		}

		public boolean charTyped(char character, int mods) {
			return this.mainRenderer.charTyped(character, mods);
		}

	}

	@FunctionalInterface
	public interface TagRendererFactory<T extends Tag> {
		TagRenderer<T> create(String tagName, T tag, TagRendererContext<T> ctx);
	}

}
