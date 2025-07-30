package net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers;

import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public abstract class TagRenderer<T extends Tag> {
	protected final Consumer<T> onTagUpdate;
	protected final Consumer<String> onEntering;
	protected final Runnable onMainTagEdited;
	private final Rect2i box = new Rect2i(0, 0, 144, 20);
	private final T tag;
	@Nullable
	private final String name;

	public TagRenderer(@Nullable String tagName, T tag, Consumer<T> onTagUpdate, Consumer<String> onEntering, Runnable onMainTagEdited) {
		this.name = tagName;
		this.tag = tag;
		this.onTagUpdate = onTagUpdate;
		this.onEntering = onEntering;
		this.onMainTagEdited = onMainTagEdited;
	}

	protected void enterInTag() {
		if (this.canEnterInTag())
			this.onEntering.accept(this.name);
	}

	protected void updateThis(T newSelf) {
		this.onTagUpdate.accept(newSelf);
	}

	public T getTag() {
		return this.tag;
	}

	public @Nullable String getName() {
		return this.name;
	}

	public Rect2i getBox() {
		return this.box;
	}

	public abstract void render(GuiUtils gui);

	public abstract boolean mouseClicked(double mouseX, double mouseY);

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

	public Tag tryWalk(Tag tag) {
		return null;
	}
}
