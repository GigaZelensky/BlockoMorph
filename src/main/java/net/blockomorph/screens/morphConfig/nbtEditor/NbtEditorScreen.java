package net.blockomorph.screens.morphConfig.nbtEditor;

import net.blockomorph.screens.AbstractScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.CompoundTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.StringTagRenderer;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ListenerEditBox;
import net.blockomorph.screens.utils.ScrollerManager;
import net.blockomorph.utils.MorphUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtEditorScreen extends AbstractScreen {
	private static final HashMap<TagType<?>, TagRendererFactory<?>> RENDERERS = new HashMap<>();
	private static final ResourceLocation CORNERS_TEXTURE = GuiUtils.res("textures/screens/nbt_editor_corners.png");
	private static final ScrollerManager.CustomBarData SCOLLER = new ScrollerManager.CustomBarData(GuiUtils.res("textures/screens/nbt_scroller.png"), 7, 15);
	private final Consumer<String> onEntering = tagName -> {
		this.enterInTag(this.path + "/" + tagName);
	};
	private final CompoundTag editingTag;
	private final Runnable onEdited;
	private EditBox tagBox;
	private int internalBoxX;
	private int internalBoxY;
	protected final int internalBoxLength = 173;
	protected final int internalBoxHeigth = 159;
	private final List<RenderableTag<?>> renderables;
	private final ScrollerManager<RenderableTag<?>> scrollerManager;
	private String path = "";
	private int frameColor = -1;

	public NbtEditorScreen(CompoundTag editingTag, Runnable onEdited) {
		super("nbt_editor_screen", new ScreenPosition(229, 191));
		this.editingTag = Objects.requireNonNull(editingTag);
		this.onEdited = Objects.requireNonNull(onEdited);
		this.renderables = new ArrayList<>(7);
		this.scrollerManager = new ScrollerManager<>(() -> this.leftPos + 181, () -> this.topPos + 41, 138, 1, 7, this.renderables, SCOLLER);
	}

	static {
		register(CompoundTag.TYPE, CompoundTagRenderer::new);
		register(StringTag.TYPE, StringTagRenderer::new);
	}

	public static <T extends Tag> void register(TagType<T> type, TagRendererFactory<T> factory) {
		if (RENDERERS.containsKey(type)) throw new IllegalArgumentException("Type " + type.getName() + " already registered!");
		RENDERERS.put(type, factory);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.render(guiGraphics, mouseX, mouseY, tick);
		int i = 0;
		for (RenderableTag<?> tag : this.renderables) {
			tag.render(this.gui, i * TagRenderer.PLATE_HEIGTH);
			i++;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
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
		return this.scrollerManager.mouseDragged(mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		if (this.forEachTag(renderer -> renderer.mouseScrolled(mouseX, mouseY, yWheelOffset))) {
			return true;
		}
		return this.scrollerManager.mouseScrolled(yWheelOffset);
	}

	@Override
	public boolean charTyped(char character, int mods) {
		if (this.forEachTag(renderer -> renderer.charTyped(character, mods))) {
			return true;
		}
		return super.charTyped(character, mods);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		if (this.forEachTag(renderer -> renderer.keyPressed(key, scancode, mods))) {
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
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, tick);
		this.scrollerManager.renderScroller(this.gui);
		this.gui.fill(this.internalBoxX, this.internalBoxY, this.internalBoxX + this.internalBoxLength, this.internalBoxY + this.internalBoxHeigth, this.frameColor);
		this.gui.blitMonoImage(CORNERS_TEXTURE, this.internalBoxX, this.internalBoxY, this.internalBoxLength, this.internalBoxHeigth);
	}

	@Override
	protected void init() {
		super.init();
		this.internalBoxX = this.leftPos + 23;
		this.internalBoxY = this.topPos + 26;
		this.tagBox = new ListenerEditBox(this.font, this.leftPos + 23, this.topPos + 5, 129, 19, this.getTitle(), value -> {}, ListenerEditBox.EDITBOX_BORDER_SPRITE);
		this.addRenderableWidget(this.tagBox);
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
			TagRenderer<?> renderer = getRendererForTag(previousName, root, null, this.onEntering, this.onEdited);
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
		TagRenderer<?> rootRenderer = getRendererForTag("root", root, null, this.onEntering, this.onEdited);
		if (rootRenderer != null && rootRenderer.canEnterInTag()) {
			this.frameColor = root == this.editingTag ? ARGB.color(127, 84, 84, 84) : rootRenderer.getFrameColor();
			List<RenderableTag<?>> renderableTags = new ArrayList<>();
			for (TagRenderer<?> renderer : rootRenderer.getEnteringTags()) {
				renderableTags.add(new RenderableTag<>(renderer));
			}
			this.scrollerManager.setMainList(renderableTags.stream().filter(RenderableTag::isValid).toList());
			this.scrollerManager.setScrollOffset(0f);
			this.scrollerManager.refreshList();
			return;
		}
		throw new IllegalArgumentException("Tag: " + root.getType().getName() + " no enterable!");
	}

	@Nullable @SuppressWarnings("unchecked")
	public static <T extends Tag> TagRenderer<T> getRendererForTag(String name, T tag, Consumer<T> onEdited, Consumer<String> onEntering, Runnable onMainTagEdited) {
		try {
			TagRendererFactory<T> renderSource = (TagRendererFactory<T>) RENDERERS.get(tag.getType());
			if (renderSource != null) {
				return renderSource.create(name, tag, onEdited, onEntering, onMainTagEdited);
			}
		} catch (ClassCastException e) {
			MorphUtils.LOGGER.error("Invalid tag registration for NBT Editor: ", e);
			return null;
		}
		return null;
	}

	private class RenderableTag<T extends Tag> {
		private final TagRenderer<T> renderer;

		private RenderableTag(TagRenderer<T> renderer) {
			this.renderer = renderer;
		}

		public boolean isValid() {
			return this.renderer != null;
		}

		public void render(GuiUtils gui, int yOffset) {
			this.renderer.getBox().setPosition(NbtEditorScreen.this.internalBoxX + 10, NbtEditorScreen.this.internalBoxY + 14 + yOffset);
			this.renderer.render(gui);
		}

		public boolean mouseClicked(double mouseX, double mouseY) {
			return this.renderer.mouseClicked(mouseX, mouseY);
		}

		public boolean mouseScrolled(double mouseX, double mouseY, double yOffsetWheel) {
			return this.renderer.mouseScrolled(mouseX, mouseY, yOffsetWheel);
		}

		public boolean keyPressed(int key, int scancode, int mods) {
			return this.renderer.keyPressed(key, scancode, mods);
		}

		public boolean charTyped(char character, int mods) {
			return this.renderer.charTyped(character, mods);
		}

	}

	@FunctionalInterface
	public interface TagRendererFactory<T extends Tag> {
		TagRenderer<T> create(String tagName, T tag, Consumer<T> onTagUpdate, Consumer<String> onEntering, Runnable onMainTagEdited);
	}

}
