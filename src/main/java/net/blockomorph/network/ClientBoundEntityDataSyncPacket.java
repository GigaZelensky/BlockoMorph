package net.blockomorph.network;

import io.netty.buffer.Unpooled;
import net.blockomorph.utils.accessors.SynchedEntity;
import net.blockomorph.utils.dataSyncher.AutoSycnhedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
		buffer.release();
		this.payload = buffer.array();
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
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			if (level.getEntity(this.entityId) instanceof SynchedEntity synchedEntity) {
				AutoSycnhedEntityData<?> data = synchedEntity.getDataById(this.location);
				data.readFromNetwork(new FriendlyByteBuf(Unpooled.wrappedBuffer(this.payload)));
			}
		}
	}
}
