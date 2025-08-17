package net.blockomorph.mixins.main;

import net.blockomorph.network.ClientBoundEntityDataSyncPacket;
import net.blockomorph.utils.dataSyncher.SyncedEntity;
import net.blockomorph.utils.dataSyncher.AutoSyncedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = Entity.class, priority = 1030)
public abstract class EntityDataSupportMixin implements SyncedEntity {

	@Shadow private int id;
	private List<AutoSyncedEntityData<?>> SYNCERS;
	private boolean dirty;

	public void registerDataSyncer(AutoSyncedEntityData<?> data) {
		if (this.getDataById(data.getId()) != null) throw new IllegalArgumentException("Data with id: " + this.id + " already registered!");
		this.get().add(data);
	}

	public void setDirty() {
		this.dirty = true;
	}

	public void checkOrSendImmediate(Consumer<ClientBoundEntityDataSyncPacket> doing, boolean force) {
		if (!this.dirty && !force) return;
		for (AutoSyncedEntityData<?> data : this.get()) {
			if (data.isDirty() || force) {
				doing.accept(new ClientBoundEntityDataSyncPacket(this, data));
			}
		}
		this.dirty = false;
	}

	@Nullable
	public AutoSyncedEntityData<?> getDataById(ResourceLocation id) {
		for (AutoSyncedEntityData<?> data : this.get()) {
			if (data.getId().equals(id)) return data;
		}
		return null;
	}

	private List<AutoSyncedEntityData<?>> get() {
		if (SYNCERS == null) {
			SYNCERS = new ArrayList<>();
		}
		return SYNCERS;
	}
}
