package net.blockomorph.mixins.main;

import net.blockomorph.network.ClientBoundEntityDataSyncPacket;
import net.blockomorph.utils.accessors.SynchedEntity;
import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(Entity.class)
public abstract class EntityDataSupportMixin implements SynchedEntity {

	@Shadow private int id;
	private final List<AutoSycnhedEntityData<?>> SYNCHERS = new ArrayList<>();

	public void registerDataSycnher(AutoSycnhedEntityData<?> data) {
		if (this.getDataById(data.getId()) != null) throw new IllegalArgumentException("Data with id: " + this.id + " already registered!");
		SYNCHERS.add(data);
	}

	public void checkOrSendImmediatle(Consumer<ClientBoundEntityDataSyncPacket> doing, boolean force) {
		for (AutoSycnhedEntityData<?> data : SYNCHERS) {
			if (data.isDirty() || force) {
				doing.accept(new ClientBoundEntityDataSyncPacket(this, data));
			}
		}
	}

	@Nullable
	public AutoSycnhedEntityData<?> getDataById(ResourceLocation id) {
		for (AutoSycnhedEntityData<?> data : SYNCHERS) {
			if (data.getId().equals(id)) return data;
		}
		return null;
	}
}
