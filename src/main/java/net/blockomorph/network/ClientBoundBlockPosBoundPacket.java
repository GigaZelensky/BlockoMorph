package net.blockomorph.network;

import net.blockomorph.utils.BlockPosBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClientBoundBlockPosBoundPacket implements BlockMorphPacket {
    public static final String ID = "client_bound_blockpos_bound_packet";
    public final UUID uuid;
    public final ChunkPos pos;

    public ClientBoundBlockPosBoundPacket(FriendlyByteBuf buffer) {
        this.uuid = buffer.readUUID();
        this.pos = buffer.readChunkPos();
    }

    public ClientBoundBlockPosBoundPacket(UUID uuid, @Nullable ChunkPos pos) {
        this.uuid = uuid;
        this.pos = pos == null ? new ChunkPos(Integer.MAX_VALUE, 0) : pos;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.uuid);
        buffer.writeChunkPos(this.pos);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void handle(Player player) {
        BlockPosBounds.handleBlockPosBound(this);
    }
}
