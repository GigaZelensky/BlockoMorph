package net.blockomorph.core;

import net.blockomorph.command.*;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.network.*;
import net.blockomorph.utils.config.*;

import net.blockomorph.utils.coords.BlockPosBounds;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MainBus {

	public static void registerClient() {
		registerMain();
		ClientPlayNetworking.registerGlobalReceiver(MainPacket.ID, (payload, context) -> {
			try {
				payload.handle(null, true);
			} catch (Exception e) {
				context.client().getConnection().getConnection().disconnect(Component.literal(e.getMessage()));
			}
		});
		KeyMappings.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
	}

	public static void registerServer() {
		registerMain();
	}

	private static void registerMain() {
		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
			BlockmorphCommand.register(dispatcher, commandBuildContext, environment);
			BlockmorphconfigCommand.register(dispatcher, commandBuildContext, environment);
		});
		ServerLifecycleEvents.SERVER_STARTING.register((sv) -> {
			Config.setServer(sv);
			BlockPosBounds.load();
		});
		PayloadTypeRegistryImpl.PLAY_C2S.register(MainPacket.ID, MainPacket.STREAM_CODEC);
		PayloadTypeRegistryImpl.PLAY_S2C.register(MainPacket.ID, MainPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MainPacket.ID, (packet, context) -> {
			try {
				packet.handle(context.player(), false);
			} catch (Exception e) {
				context.player().connection.disconnect(Component.literal(e.getMessage()));
			}
		});
		MorphUtils.registerPacket(ClientBoundConfigUpdatePacket.ID, ClientBoundConfigUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundMorphUpdatePacket.ID, ClientBoundMorphUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundBlockPosBoundPacket.ID, ClientBoundBlockPosBoundPacket::new, true);
		MorphUtils.registerPacket(ClientBoundServerBlockEntityTagPacket.ID, ClientBoundServerBlockEntityTagPacket::new, true);
		MorphUtils.registerPacket(ClientBoundApplyBlockMorphPacket.ID, ClientBoundApplyBlockMorphPacket::new, true);
		MorphUtils.registerPacket(ClientBoundEntityDataSyncPacket.ID, ClientBoundEntityDataSyncPacket::new, true);

		MorphUtils.registerPacket(ServerBoundBlockMorphPacket.ID, ServerBoundBlockMorphPacket::new, false);
		MorphUtils.registerPacket(ServerBoundConfigUpdatePacket.ID, ServerBoundConfigUpdatePacket::new, false);
		MorphUtils.registerPacket(ServerBoundSelfNbtRequestPacket.ID, ServerBoundSelfNbtRequestPacket::new, false);
	}
}
