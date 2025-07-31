package net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public abstract class TagRenderer<T extends Tag> {
	public static final int PLATE_SPRITE_LENGTH = 194;
	public static final int PLATE_SPRITE_HEIGTH = 200;
	public static final int PLATE_LENGTH = 144;
	public static final int PLATE_HEIGTH = 20;
	protected static final ResourceLocation TAGS_SPRITE = GuiUtils.res("textures/screens/tags.png");
	protected final Consumer<T> onTagUpdate;
	protected final Consumer<String> onEntering;
	protected final Runnable onMainTagEdited;
	protected final Rect2i box = new Rect2i(0, 0, PLATE_LENGTH, PLATE_HEIGTH);
	private final T tag;
	private final String name;
	private boolean nameVisibility = true;

	public TagRenderer(String tagName, T tag, Consumer<T> onTagUpdate, Consumer<String> onEntering, Runnable onMainTagEdited) {
		this.name = tagName;
		this.tag = tag;
		this.onTagUpdate = onTagUpdate;
		this.onEntering = onEntering;
		this.onMainTagEdited = onMainTagEdited;
	}

	public void setNameVisibility(boolean yes) {
		this.nameVisibility = yes;
	}

	protected void enterInTag() {
		if (this.canEnterInTag())
			this.onEntering.accept(this.name);
	}

	protected void updateThis(T newSelf) {
		this.onTagUpdate.accept(newSelf);
	}

	protected void renderPlate(GuiUtils gui, int number) {
		gui.blit(TAGS_SPRITE, this.box.getX(), this.box.getY(), 0, number * 20, PLATE_LENGTH, PLATE_HEIGTH, PLATE_SPRITE_LENGTH, PLATE_SPRITE_HEIGTH);
	}

	public T getTag() {
		return this.tag;
	}

	public String getName() {
		return this.name;
	}

	public Rect2i getBox() {
		return this.box;
	}

	public abstract void render(GuiUtils gui);

	public boolean mouseClicked(double mouseX, double mouseY) {
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double yOffsetWheel) {
		return false;
	}

	public boolean keyPressed(int key, int scancode, int mods) {
		return false;
	}

	public boolean charTyped(char character, int mods) {
		return false;
	}

	public boolean canEnterInTag() {
		return false;
	}

	public int getFrameColor() {
		return -1;
	}

	public List<TagRenderer<?>> getEnteringTags() {
		return List.of();
	}

	public Tag tryWalk(String nameElementInThisTag) {
		return null;
	}
}
