package net.blockomorph.screens.morphConfig.nbtEditor;

import net.blockomorph.screens.AbstractScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.TagRendererContext;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays.TagEditingOverlay;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ListenerEditBox;
import net.blockomorph.screens.utils.ScrollerManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class NbtEditorScreen extends AbstractScreen {
	private static final ScrollerManager.CustomBarData SCOLLER = new ScrollerManager.CustomBarData(GuiUtils.res("textures/screens/nbt_scroller.png"), 7, 15);
	private final BiConsumer<String, Boolean> onEntering = (tagName, intr) -> {
		this.enterInTag(this.path + "/" + tagName, intr);
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
	private TagRenderer<?> currentEnteredTag;
	private TagEditingOverlay overlay;
	private final Consumer<TagEditingOverlay> onTagReceived;

	public NbtEditorScreen(CompoundTag editingTag, Consumer<CompoundTag> onEdited) {
		super("nbt_editor_screen", new ScreenPosition(229, 191));
		this.editingTag = Objects.requireNonNull(editingTag).copy();
		this.onEdited = Objects.requireNonNull(onEdited);
		this.renderables = new ArrayList<>(7);
		this.scrollerManager = new ScrollerManager<>(() -> this.leftPos + 181, () -> this.topPos + 41, 138, 1, 7, this.renderables, SCOLLER);
		this.onTagEdited = () -> this.onEdited.accept(this.editingTag);
		this.onTagReceived = this::setOverlay;
	}

	private void setOverlay(TagEditingOverlay tagEditor) {
		this.overlay = tagEditor;
		if (tagEditor != null) {
			tagEditor.init(this.width, this.height, this::setOverlay);
		}
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
			this.gui.renderInDepthIfNeededAfterBlockRendering(() -> {
				this.gui.blurScreen(this.width, this.height, 190);
				this.overlay.render(this.gui);
			});
		} else if (tooltip != null) {
			this.gui.renderTooltip(tooltip, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (this.overlay != null) {
			return this.overlay.mouseClicked(mouseX, mouseY, type);
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
			return this.overlay.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
		} else if (this.scrollerManager.mouseDragged(mouseY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, type, mouseXOffset, mouseYOffset);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		if (this.overlay != null) {
			return this.overlay.mouseScrolled(mouseX, mouseY, xWheelOffset, yWheelOffset);
		} else if (this.forEachTag(renderer -> renderer.mouseScrolled(mouseX, mouseY, yWheelOffset))) {
			return true;
		}
		return this.scrollerManager.mouseScrolled(yWheelOffset);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int type) {
		this.scrollerManager.disableScrollWork();
		if (this.overlay != null) {
			this.overlay.mouseReleased(mouseX, mouseY, type);
		}
		return super.mouseReleased(mouseX, mouseY, type);
	}

	@Override
	public boolean charTyped(char character, int mods) {
		if (this.overlay != null) {
			return this.overlay.charTyped(character, mods);
		} else if (this.forEachTag(renderer -> renderer.charTyped(character, mods))) {
			return true;
		}
		return super.charTyped(character, mods);
	}

	@Override
	public boolean keyPressed(int key, int scancode, int mods) {
		if (this.overlay != null) {
			if (key == 256) {
				this.onTagReceived.accept(null);
				return true;
			}
			return this.overlay.keyPressed(key, scancode, mods);
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
		int color = this.path.isEmpty() ? ARGB.color(255, 84, 84, 84) : this.currentEnteredTag.getFrameColor();
		this.gui.fill(this.internalBoxX, this.internalBoxY, this.internalBoxX + this.internalBoxLength, this.internalBoxY + this.internalBoxHeigth, color);
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
				this.enterInTag(newPath, false);
			}
		}).bounds(this.internalBoxX + 146, this.internalBoxY + 4, 22, 9).build();
		this.addRenderableWidget(this.pathExit);
		this.initList(false);
		if (this.overlay != null) {
			this.overlay.init(this.width, this.height, this::setOverlay);
		}
	}

	public void enterInTag(String path, boolean intr) {
		this.path = path;
		this.initList(intr);
	}

	private void initList(boolean interpatitationEnd) {
		Tag root = this.editingTag;
		String previousName = "root";
		for (Iterator<String> it = Arrays.stream(this.path.split("/")).filter(v -> !v.isEmpty()).iterator(); it.hasNext();) {
			String tagName = it.next();
			TagRenderer<?> renderer = TagTypes.getRendererForTag(previousName, root, new TagRendererContext<>(this.provider(), this.onEntering, this.onTagEdited, this.onTagReceived));
			if (renderer != null) {
				//renderer = Objects.requireNonNullElse(renderer.getInterpretationRenderer(null), renderer);
				Tag child = renderer.tryWalk(tagName);
				if (child != null) {
					root = child;
					previousName = tagName;
					continue;
				}
			}
			throw new IllegalArgumentException("Illegal path: " + this.path + " for tag: " + this.editingTag);
		}
		TagRenderer<?> rootRenderer = TagTypes.getRendererForTag("root", root, new TagRendererContext<>(this.provider(), this.onEntering, this.onTagEdited, this.onTagReceived));
		if (rootRenderer != null) {
			if (interpatitationEnd) rootRenderer = Objects.requireNonNullElse(rootRenderer.getInterpretationRenderer(null), rootRenderer);
			if (rootRenderer.canEnterInTag()) {
				this.currentEnteredTag = rootRenderer;
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
		}
		throw new IllegalArgumentException("Tag: " + root.getType().getName() + " no enterable!");
	}

	private HolderGetter.Provider provider() {
		return GuiUtils.MC.level != null ? GuiUtils.MC.level.registryAccess() : RegistryAccess.EMPTY;
	}

	private class RenderableTag<T extends Tag> {
		private TagRenderer<T> mainRenderer;

		private RenderableTag(TagRenderer<T> renderer) {
			this.mainRenderer = renderer;
			if (this.mainRenderer != null) {
				TagRenderer<T> additionalRenderer = renderer.getInterpretationRenderer(() -> {
					renderer.forceTagChange(this.mainRenderer.getTag());
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
