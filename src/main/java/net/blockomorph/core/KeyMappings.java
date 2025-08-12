package net.blockomorph.core;

import net.blockomorph.screens.BlockMorphConfigScreen;
import net.blockomorph.screens.ConfigScreen;
import net.blockomorph.screens.MorphScreen;
import net.blockomorph.utils.config.Config;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class KeyMappings {
    private static final Minecraft mc = Minecraft.getInstance();
	private static final ArrayList<KeyMapping> KEYS = new ArrayList<>();

	public static final KeyMapping MORPH = new HandlerKeymapping("key.blockomorph.morph_menu", GLFW.GLFW_KEY_Y, () ->
		mc.setScreen(new MorphScreen(Config.Mode.NONE, false))
	);

	public static final KeyMapping MORPH_CONFIG = new HandlerKeymapping("key.blockomorph.morph_config_menu", GLFW.GLFW_KEY_U, () ->
		mc.setScreen(new BlockMorphConfigScreen(false))
	);

	public static final KeyMapping CONFIG = new HandlerKeymapping("key.blockomorph.config_menu", GLFW.GLFW_KEY_N, () -> {
		if (canOpenConfig()) {
			mc.setScreen(new ConfigScreen());
		}
	});

	/*public static final KeyMapping db = new HandlerKeymapping("key.blockomorph.debug", GLFW.GLFW_KEY_I, () -> {
		if (mc.hitResult instanceof MorphedPlayerHitResult hit) {
			hit.getPlayer().getBlocksData2().forEach(((inPlayerBlockPos, blockInPlayer2) -> {
				System.out.println(inPlayerBlockPos + "   " + blockInPlayer2.getBlockState());
			}));
		}
	});

	public static final KeyMapping db2 = new HandlerKeymapping("key.blockomorph.debug2", GLFW.GLFW_KEY_J, () -> {
		BlockPos pos = InPlayerBlockPos.get(0, 0, 0).boundedBlockPos(mc.player);
		System.out.println("setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " ");
		BlockPosBounds.CACHE.forEach(((chunkPos, player) -> {
			System.out.println(chunkPos);
		}));
		BlockPosBounds.CLIENT_CACHE.forEach(((chunkPos, player) -> {
			System.out.println(chunkPos);
		}));
		for (Entity ent2 : mc.level.entitiesForRendering()) {
			if (ent2 instanceof Player a)
				System.out.println(ent2.getName() + "  :  " + BlockPosBounds.getChunkPosForPlayer(a));
		}
		PlayerAccessor.of(mc.player).getBlocksData2().forEach(((inPlayerBlockPos, blockInPlayer2) -> {
			System.out.println(inPlayerBlockPos + "    " + blockInPlayer2.getBlockState());
		}));
	});

	public static final KeyMapping db3 = new HandlerKeymapping("key.blockomorph.debug3", GLFW.GLFW_KEY_V, () -> {
		//if (Minecraft.getInstance().hitResult instanceof EntityHitResult ent) {
        //    MorphUtils.sendServer(new DebugPacket2(ent.getEntity().getId()));
		MorphUtils.sendServer(new DebugPacket2(-1));
        //}
	});*/

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		for (KeyMapping key : KEYS) {
			event.register(key);
		}
	}

	private static boolean canOpenConfig() {
		return mc.player != null && mc.player.hasPermissions(2) && (boolean)Config.getInstance().getValue("canOperatorModifyConfig");
	}

	private static class HandlerKeymapping extends KeyMapping {
		private final Runnable action;

		public HandlerKeymapping(String lang, int key, Runnable action) {
			super(lang, key, "key.categories.ui");
			KEYS.add(this);
			this.action = action;
		}

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDown && mc.screen == null) {
				this.action.run();
			}
		}
	}

}
