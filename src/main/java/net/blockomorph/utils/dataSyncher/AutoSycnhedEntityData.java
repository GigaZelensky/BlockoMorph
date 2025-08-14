package net.blockomorph.utils.dataSyncher;

import net.blockomorph.utils.accessors.SynchedEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public abstract class AutoSycnhedEntityData<DATA> {
	protected DATA data;
	protected final ResourceLocation id;
	protected boolean isDirty;

	protected AutoSycnhedEntityData(Entity entity, ResourceLocation id, DATA defaultValue) {
		if (entity instanceof SynchedEntity synchedEntity) {
			synchedEntity.registerDataSycnher(this);
			this.id = id;
			this.data = Objects.requireNonNull(defaultValue);
		} else throw new IllegalStateException("Cannot register entity syncer from blockomorph! Most likely mixin not applied or missing! Class: " + entity.getClass() + " Id: " + id);
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public final void writeInNetwork(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(this.id);
		this.writeInBuffer(buffer);
		this.isDirty = false;
	}

	public final void readFromNetwork(FriendlyByteBuf buffer) {
		this.readFromBuffer(buffer);
	}

	protected abstract void readFromBuffer(FriendlyByteBuf buffer);

	protected abstract void writeInBuffer(FriendlyByteBuf buffer);

	public void set(DATA data) {
		if (!this.data.equals(data)) {
			this.data = data;
			this.isDirty = true;
		}
	}

	public DATA get() {
		return this.data;
	}
}
