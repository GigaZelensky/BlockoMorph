package net.blockomorph.network;

import net.blockomorph.screens.morph.AbstractMorphScreen;
import net.blockomorph.utils.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class ClientBoundConfigUpdatePacket implements BlockMorphPacket {
	public static final String ID = "client_bound_config_update_packet";
	Config config;
	public ClientBoundConfigUpdatePacket(FriendlyByteBuf buf) {
		this.config = Config.readFromBuffer(buf);
	}

	public ClientBoundConfigUpdatePacket(Config cfg) {
		this.config = cfg;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		this.config.writeInBuffer(buffer);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void handle(Player player) {
		if (!Minecraft.getInstance().isLocalServer())
			Config.loadExternal(this.config);
		if (Minecraft.getInstance().screen instanceof AbstractMorphScreen gui) {
			gui.BLOCKS_MANAGER.updateAllowedBlocks();
		}
	}
}
