package net.blockomorph.core;

import net.blockomorph.BlockomorphServer;
import net.blockomorph.command.BlockmorphCommand;
import net.blockomorph.command.BlockmorphconfigCommand;
import net.blockomorph.command.EnumArgument;
import net.blockomorph.network.*;
import net.blockomorph.screens.PlayerCrackOverlay;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.coords.BlockPosBounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class MainBus {

	@Environment(value = EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(MainPacket.ID, (client, handler, buf, responseSender) -> {
			try {
				Runnable run = MainPacket.preApply(buf, null, true);
				client.execute(run);
			} catch (Exception e) {
				handler.getConnection().disconnect(Component.literal(e.getMessage()));
			}
		});
		HudRenderCallback.EVENT.register(PlayerCrackOverlay::render);
		KeyMappings.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
		registerMain();
	}

	public static void registerServer() {
		registerMain();
	}

	private static void registerMain() {
		ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(BlockomorphServer.MOD_ID, "enum_argument"), EnumArgument.class, new EnumArgument.ContextInfo());
		CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, environment) -> {
			BlockmorphCommand.register(dispatcher, commandBuildContext, environment);
			BlockmorphconfigCommand.register(dispatcher, commandBuildContext, environment);
		});
		ServerPlayNetworking.registerGlobalReceiver(MainPacket.ID, (server, player, handler, buf, responseSender) -> {
			try {
				Runnable run = MainPacket.preApply(buf, player, false);
				server.execute(run);
			} catch (Exception e) {
				handler.disconnect(Component.literal(e.getMessage()));
			}
		});
		ServerLifecycleEvents.SERVER_STARTING.register((sv) -> {
			Config.setServer(sv);
			BlockPosBounds.load();
		});
		MorphUtils.registerPacket(ClientBoundConfigUpdatePacket.ID, ClientBoundConfigUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundMorphUpdatePacket.ID, ClientBoundMorphUpdatePacket::new, true);
		MorphUtils.registerPacket(ClientBoundBlockPosBoundPacket.ID, ClientBoundBlockPosBoundPacket::new, true);
		MorphUtils.registerPacket(ClientBoundServerBlockEntityTagPacket.ID, ClientBoundServerBlockEntityTagPacket::new, true);

		MorphUtils.registerPacket(ServerBoundBlockMorphPacket.ID, ServerBoundBlockMorphPacket::new, false);
		MorphUtils.registerPacket(ServerBoundConfigUpdatePacket.ID, ServerBoundConfigUpdatePacket::new, false);
	}

}
