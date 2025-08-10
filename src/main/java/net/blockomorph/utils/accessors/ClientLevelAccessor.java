package net.blockomorph.utils.accessors;

import net.minecraft.world.level.Level;

public interface ClientLevelAccessor {
	boolean specialRenderingMode();
	void setSpecialRenderingMode(boolean yes);

	static ClientLevelAccessor of(Level lv) {
		if (lv instanceof ClientLevelAccessor acc)
			return acc;
		return NULL;
	}

	ClientLevelAccessor NULL = new ClientLevelAccessor() {
		@Override
		public boolean specialRenderingMode() { return false; }
		@Override
		public void setSpecialRenderingMode(boolean yes) {}
	};
}
