package net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays;

import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.blockomorph.screens.morphConfig.nbtEditor.TagTypes;
import net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers.TagRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.screens.utils.ObjectListRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TagAddingOverlay<T extends Tag> extends TagEditingOverlay {
	private static final ResourceLocation MENU = GuiUtils.res("textures/screens/tag_adding_screen.png");
	private static final int imageLength = 159;
	private static final int imageHeight = 59;
	private int tagBoxX;
	private int tagBoxY;
	private final ObjectListRenderer<TagTypes, TagType<? extends T>> selector;
	private final BiConsumer<String, T> onTagCreated;
	private CachedType<? extends T> currentType;
	private final TagType<? extends T> first;
	private int leftPos;
	private int topPos;
	private final boolean listDisabled;
	private final Predicate<String> filter;
	private EditBox value;
	private Button done;

	@SafeVarargs
	public TagAddingOverlay(Predicate<String> nameFilter, BiConsumer<String, T> onTagCreated, TagType<? extends T>... types) {
		if (types.length == 0) throw new IllegalArgumentException("Types array is empty!");
		this.selector = new ObjectListRenderer<>(-1, -256, 7, (tagTypes, tagType) -> {
			return tagType.getPrettyName().replace("TAG_", "").replace("_", " ");
		}, tagTypes -> {
			return List.of(types);
		});
		this.onTagCreated = onTagCreated;
		this.listDisabled = types.length == 1;
		this.filter = nameFilter;
		this.first = types[0];
	}

	private <TAG extends T> void setCurrentType(TagType<TAG> type) {
		TagTypes.TagCreator<TAG> creator = TagTypes.createTag(type);
		this.currentType = new CachedType<>(type, creator, TagTypes.getRendererForTag(null, Objects.requireNonNull(creator).defaultValue(), null));
		this.currentType.renderer.getBox().setPosition(this.tagBoxX, this.tagBoxY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (type == 0) {
			if (!this.listDisabled) {
				int x = this.tagBoxX;
				int y = this.tagBoxY;
				if (this.selector.mouseClicked(mouseX, mouseY)) {
					return true;
				} else if (GuiUtils.isMouseOver(x, y, x + 54, y + 20, mouseX, mouseY)) {
					this.selector.drop(null, tagType -> {
						GuiUtils.playClickSound();
						this.setCurrentType(tagType);
						this.recheckValueField();
						this.done.setMessage(CommonComponents.GUI_DONE);
					});
					GuiUtils.playClickSound();
					if (this.done.getWidth() < 60) this.done.setMessage(CommonComponents.EMPTY);
					return true;
				}
				this.selector.close();
				this.done.setMessage(CommonComponents.GUI_DONE);
			}
			return super.mouseClicked(mouseX, mouseY, 0);
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		return this.selector.mouseScrolled(mouseX, mouseY, yWheelOffset);
	}

	@Override
	protected void renderInGui(GuiUtils gui) {
		int x = this.tagBoxX + 6;
		int y = this.tagBoxY + 6;
		this.selector.renderName(gui, x, y, 38, null, this.currentType.type, -1);
		this.selector.render(gui, x, y, -1, 10);

	}

	@Override
	protected void renderBackground(GuiUtils gui) {
		TagRenderer<?> renderer = this.currentType.renderer;
		gui.blitMonoImage(MENU, this.leftPos, this.topPos, imageLength, imageHeight);

		gui.enableScrissors(this.tagBoxX, this.tagBoxY, this.tagBoxX + 50, this.tagBoxY + 20);
		renderer.renderPlateWithoutCtx(gui);
		gui.disableScrissors();

		int x = this.tagBoxX + 50;
		gui.enableScrissors(x, this.tagBoxY, x + 4, this.tagBoxY + 20);
		int plateX = x - (renderer.getBox().getWidth() - 4);
		int oldX = renderer.getBox().getX();
		renderer.getBox().setX(plateX);
		renderer.renderPlateWithoutCtx(gui);
		renderer.getBox().setX(oldX);
		gui.disableScrissors();

		gui.blit(NbtEditorScreen.BUTTONS_SPRITE, this.tagBoxX + 4, this.tagBoxY + 4, 0, 32, 46, 11, NbtEditorScreen.BUTTON_SPRITE_LENGTH, NbtEditorScreen.BUTTON_SPRITE_HEIGTH);
	}

	private void recheckValueField() {
		boolean noValue = this.currentType.creator.noHasValue();
		if (noValue) {
			this.value.visible = false;
			this.done.setX(this.leftPos + 7);
			this.done.setWidth(146);
			this.value.setValue("");
		} else {
			this.value.visible = true;
			this.done.setX(this.leftPos + 99);
			this.done.setWidth(54);
			Tag tag = this.currentType.creator.create(this.value.getValue());
			if (tag == null) this.value.setValue("");
		}
	}

	@Override
	public void init(int width, int height, Consumer<TagEditingOverlay> onChange) {
		super.init(width, height, onChange);
		this.leftPos = (this.width - imageLength) / 2;
		this.topPos = (this.height - imageHeight) / 2;
		this.tagBoxX = this.leftPos + 99;
		this.tagBoxY = this.topPos + 7;

		this.setCurrentType(this.first);

		EditBox name = new EditBox(GuiUtils.MC.font, this.leftPos + 7, this.topPos + 7, 89, 20, Component.literal("name"));
		name.setFilter(this.filter);
		this.addRenderableWidget(name);

		this.value = new EditBox(GuiUtils.MC.font, this.leftPos + 7, this.topPos + 32, 89, 20, Component.literal("name"));
		this.value.setFilter(value -> {
			if (value.isEmpty()) return true;
			Tag tag = this.currentType.creator.create(value);
			return tag != null;
		});
		this.addRenderableWidget(this.value);

		this.done = new Button(0, this.topPos + 32, 0, 20, CommonComponents.GUI_DONE, b -> {
			String value = this.value.getValue();
			T tag = this.currentType.creator.create(value);
			this.onTagCreated.accept(name.getValue(), value.isEmpty() ? this.currentType.creator.defaultValue() : tag);
			onChange.accept(null);
		}, Supplier::get) {
			@Override
			protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
				this.isHovered = this.isMouseOver(mouseX, mouseY) && !TagAddingOverlay.this.selector.isListFocused(mouseX, mouseY);
				super.renderWidget(guiGraphics, mouseX, mouseY, tick);
			}
		};
		this.addRenderableWidget(this.done);

		this.recheckValueField();
	}

	private record CachedType<T extends Tag>(TagType<T> type, TagTypes.TagCreator<T> creator, TagRenderer<T> renderer) {}
}
