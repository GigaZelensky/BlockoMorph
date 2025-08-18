package net.blockomorph.network;

import net.blockomorph.utils.BlockInPlayer2;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;

public class ServerBoundMorphActionPacket implements BlockMorphPacket {
	public static final String ID = "server_bound_morph_action_packet";
	public static final ServerBoundMorphActionPacket TNT_ACTION = new ServerBoundMorphActionPacket(Action.TNT, new CompoundTag());
	Action action;
	CompoundTag payload;

	public ServerBoundMorphActionPacket(FriendlyByteBuf buffer) {
		this.action = buffer.readEnum(Action.class);
		this.payload = buffer.readNbt();
	}

	private ServerBoundMorphActionPacket(Action action, CompoundTag payload) {
		this.action = action;
		this.payload = payload;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(this.action);
		buffer.writeNbt(this.payload);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void handle(Player player) {
		PlayerAccessor pl = PlayerAccessor.of(player);
		switch (this.action) {
			case TNT -> pl.setTnt();
			case OPEN_CONTAINER -> {
				InPlayerBlockPos pos = InPlayerBlockPos.parseBlockPos(this.payload.getStringOr("coords", ""));
				if (pos != null) {
					BlockInPlayer2 block = pl.getBlocksData2().get(pos);
					if (block != null) {
						try {
							MenuProvider provider = block.getBlockState().getMenuProvider(player.level(), block.getPos());
							player.openMenu(provider);
						} catch (Exception ignored) {}
					}
				}
			}
			default -> throw new IncompatibleClassChangeError("Irregular action type!");
		}
	}

	public static ServerBoundMorphActionPacket openContainer(InPlayerBlockPos pos) {
		CompoundTag tag = new CompoundTag();
		tag.putString("coords", pos.string());
		return new ServerBoundMorphActionPacket(Action.OPEN_CONTAINER, tag);
	}

	public enum Action {
		TNT,
		OPEN_CONTAINER
	}
}
