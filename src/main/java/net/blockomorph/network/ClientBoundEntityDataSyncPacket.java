package net.blockomorph.network;

import io.netty.buffer.Unpooled;
import net.blockomorph.utils.dataSyncher.SynchedEntity;
import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ClientBoundEntityDataSyncPacket implements BlockMorphPacket {
	public static final String ID = "client_bound_entity_data_sync_packet";
	byte[] payload;
	int entityId;
	ResourceLocation location;


	public ClientBoundEntityDataSyncPacket(FriendlyByteBuf buffer) {
		this.entityId = buffer.readInt();
		this.location = buffer.readResourceLocation();
		this.payload = buffer.readByteArray();
	}

	public ClientBoundEntityDataSyncPacket(SynchedEntity entity, AutoSycnhedEntityData<?> data) {
		this.entityId = entity.toEntity().getId();
		this.location = data.getId();
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
		data.writeInNetwork(buffer);
		this.payload = buffer.array();
		buffer.release();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.entityId);
		buffer.writeResourceLocation(this.location);
		buffer.writeByteArray(this.payload);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void handle(Player player) {
		if (Minecraft.getInstance().level.getEntity(this.entityId) instanceof SynchedEntity synchedEntity) {
			AutoSycnhedEntityData<?> data = synchedEntity.getDataById(this.location);
			data.readFromNetwork(new FriendlyByteBuf(Unpooled.wrappedBuffer(this.payload)));
			data.onReceived();
		}
	}
}
