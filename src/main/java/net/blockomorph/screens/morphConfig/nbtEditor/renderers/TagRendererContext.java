package net.blockomorph.screens.morphConfig.nbtEditor.renderers;

import net.blockomorph.screens.morphConfig.nbtEditor.renderers.overlays.TagEditingOverlay;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record TagRendererContext<T extends Tag>(
		HolderGetter.Provider provider,
		@Nullable Consumer<T> onTagUpdate,
		Consumer<String> onEntering,
		Runnable onMainTagEdited,
		Consumer<TagEditingOverlay> onTagEditingRequested,
		@Nullable Runnable onInterpretationBrake
) {

	public TagRendererContext(HolderGetter.Provider provider, Consumer<String> onEntering, Runnable onMainTagEdited, Consumer<TagEditingOverlay> onTagEditingRequested) {
		this(provider, null, onEntering, onMainTagEdited, onTagEditingRequested, null);
	}

	public <TYPE extends Tag> TagRendererContext<TYPE> withTagUpdateListener(Consumer<TYPE> type) {
		return new TagRendererContext<>(this.provider, type, this.onEntering(), this.onMainTagEdited(), this.onTagEditingRequested(), this.onInterpretationBrake());
	}

	public TagRendererContext<T> forInterpretation(Runnable onInterpretationBrake) {
		return new TagRendererContext<>(this.provider, this.onTagUpdate, this.onEntering, this.onMainTagEdited, this.onTagEditingRequested, onInterpretationBrake);
	}
}
