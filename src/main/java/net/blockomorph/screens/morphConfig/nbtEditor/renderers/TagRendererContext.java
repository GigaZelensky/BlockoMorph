package net.blockomorph.screens.morphConfig.nbtEditor.renderers;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record TagRendererContext<T extends Tag>(
		@Nullable Consumer<T> onTagUpdate,
		Consumer<String> onEntering,
		Runnable onMainTagEdited,
		@Nullable Runnable onInterpretationBrake
) {

	public TagRendererContext(Consumer<String> onEntering, Runnable onMainTagEdited) {
		this(null, onEntering, onMainTagEdited, null);
	}

	public TagRendererContext<T> withTagUpdateListener(Consumer<T> onTagUpdate) {
		return new TagRendererContext<>(onTagUpdate, this.onEntering, this.onMainTagEdited, this.onInterpretationBrake);
	}

	public TagRendererContext<T> withInterpretationStopListener(Runnable onInterpretationBrake) {
		return new TagRendererContext<>(this.onTagUpdate, this.onEntering, this.onMainTagEdited, onInterpretationBrake);
	}
}
