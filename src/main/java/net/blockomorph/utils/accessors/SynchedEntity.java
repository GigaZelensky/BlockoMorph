package net.blockomorph.utils.accessors;

import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.world.entity.Entity;

public interface SynchedEntity {

	void registerDataSycnher(AutoSycnhedEntityData<?> data);

	static SynchedEntity of(Entity entity) {
		return (SynchedEntity) entity;
	}
}
