package net.blockomorph.utils.dataSyncher;

import net.blockomorph.network.ClientBoundEntityDataSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public interface SynchedEntity {

	void registerDataSycnher(AutoSycnhedEntityData<?> data);
	AutoSycnhedEntityData<?> getDataById(ResourceLocation id);
	void checkOrSendImmediatle(Consumer<ClientBoundEntityDataSyncPacket> doing, boolean force);
	void setDirty();


	static SynchedEntity of(Entity entity) {
		return (SynchedEntity) entity;
	}

	default Entity toEntity() {
		return (Entity) this;
	}
}
