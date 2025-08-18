package net.blockomorph.utils.accessors;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.injection.Inject;

public interface ScreenAccessor {

	void setReturnable(Runnable doing);
	Runnable getReturnable();

	static ScreenAccessor of(Screen screen) {
		return (ScreenAccessor) screen;
	}
}
