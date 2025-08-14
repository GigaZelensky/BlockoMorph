package net.blockomorph.utils.accessors;

import net.blockomorph.network.ClientBoundEntityDataSyncPacket;
import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public interface SynchedEntity {

	void registerDataSycnher(AutoSycnhedEntityData<?> data);
	AutoSycnhedEntityData<?> getDataById(ResourceLocation id);
	void checkOrSendImmediatle(Consumer<ClientBoundEntityDataSyncPacket> doing, boolean force);


	static SynchedEntity of(Entity entity) {
		return (SynchedEntity) entity;
	}

	default Entity toEntity() {
		return (Entity) this;
	}
}
