package net.blockomorph.utils.dataSyncher;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TagSycnhedData extends AutoSycnhedEntityData<CompoundTag> {

	public TagSycnhedData(Entity entity, ResourceLocation id, CompoundTag defaultValue, Runnable onSynced) {
		super(entity, id, defaultValue.copy(), onSynced);
	}

	@Override
	protected void readFromBuffer(FriendlyByteBuf buffer) {
		this.data = buffer.readNbt();
	}

	@Override
	protected void writeInBuffer(FriendlyByteBuf buffer) {
		buffer.writeNbt(this.data);
	}
}
