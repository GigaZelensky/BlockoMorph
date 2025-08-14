package net.blockomorph.mixins.main;

import net.blockomorph.utils.accessors.SynchedEntity;
import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public class EntityDataSupportMixin implements SynchedEntity {
	private final List<AutoSycnhedEntityData<?>> SYNCHERS = new ArrayList<>();

	public void registerDataSycnher(AutoSycnhedEntityData<?> data) {
		SYNCHERS.add(data);
	}
}
