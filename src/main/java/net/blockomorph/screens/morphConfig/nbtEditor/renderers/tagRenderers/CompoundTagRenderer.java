package net.blockomorph.screens.morphConfig.nbtEditor.renderers.tagRenderers;

import net.blockomorph.screens.morphConfig.nbtEditor.NbtEditorScreen;
import net.blockomorph.screens.utils.GuiUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CompoundTagRenderer extends TagRenderer<CompoundTag> {

	public CompoundTagRenderer(@Nullable String tagName, CompoundTag tag, Consumer<CompoundTag> onTagUpdate, Consumer<String> onEntering, Runnable onMainTagEdited) {
		super(tagName, tag, onTagUpdate, onEntering, onMainTagEdited);
	}

	@Override
	public void render(GuiUtils gui) {

	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY) {
		return false;
	}

	@Override
	public boolean canEnterInTag() {
		return true;
	}

	@Override
	public int getFrameColor() {
		return ARGB.color(127, 245, 255, 59);
	}

	@Override
	public List<TagRenderer<?>> getEnteringTags() {
		List<TagRenderer<?>> renderers = new ArrayList<>();
		for (String key : this.getTag().keySet()) {
			Tag tag = this.getTag().get(key);
			if (tag == null) throw new NullPointerException();
			renderers.add(NbtEditorScreen.getRendererForTag(key, tag, newTag -> {
				this.getTag().put(key, newTag);
				this.onMainTagEdited.run();
			}, this.onEntering, this.onMainTagEdited));
		}
		return renderers;
	}

	@Override
	public Tag tryWalk(Tag tag) {
		if (tag instanceof CompoundTag tg) {
			return tg.get(this.getName());
		}
		return null;
	}
}
