package net.blockomorph;

import net.blockomorph.network.*;
import net.blockomorph.utils.MorphUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Blockomorph.MODID)
public class Blockomorph {
	public static final String MODID = "blockomorph";
	public static final String PROTOCOL_VERSION = "6";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

	public Blockomorph() {
		PACKET_HANDLER.registerMessage(0, MainPacket.class, MainPacket::write, MainPacket::new, MainPacket::handler);
		MorphUtils.registerPacket(ClientBoundConfigUpdatePacket.ID, ClientBoundConfigUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundBlockPosBoundPacket.ID, ClientBoundBlockPosBoundPacket::new, true);
		MorphUtils.registerPacket(ClientBoundMorphUpdatePacket.ID, ClientBoundMorphUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundServerBlockEntityTagPacket.ID, ClientBoundServerBlockEntityTagPacket::new, true);
		MorphUtils.registerPacket(ClientBoundApplyBlockMorphPacket.ID, ClientBoundApplyBlockMorphPacket::new, true);
		MorphUtils.registerPacket(ClientBoundEntityDataSyncPacket.ID, ClientBoundEntityDataSyncPacket::new, true);
		MorphUtils.registerPacket(ServerBoundBlockMorphPacket.ID, ServerBoundBlockMorphPacket::new, false);
		MorphUtils.registerPacket(ServerBoundConfigUpdatePacket.ID, ServerBoundConfigUpdatePacket::new, false);
		MorphUtils.registerPacket(ServerBoundSelfNbtRequestPacket.ID, ServerBoundSelfNbtRequestPacket::new, false);
	}

}
