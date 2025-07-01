package net.blockomorph.utils;

import net.blockomorph.BlockomorphServer;
import net.blockomorph.network.*;
import net.blockomorph.utils.config.*;


import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Direction;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderType;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.core.BlockPos;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

public class MorphUtils {
	public static final ResourceKey<DamageType> PLAYER_DESTROYED = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("blockomorph", "player_destroyed"));
	public static final ResourceKey<DamageType> PLAYER_DESTROYED_NULL = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("blockomorph", "player_destroyed_null"));
	private static boolean attackPressed;
	public static final SavedBlockManager bmanager = getSavedManager();
	public static BlockPos hitPart;
	public static Entity hitEntity;
	public static EntityHitResult hit;

	private static final HashMap<ResourceLocation, PacketInfo> handlers = new HashMap<>();
	public static PacketInfo getHandler(ResourceLocation id) {
		return handlers.get(id);
	}

	public static void sendServer(BlockMorphPacket packet) {
		ClientPlayNetworking.send(new MainPacket(packet));
	}

	public static void sendAll(BlockMorphPacket packet) {
		for (ServerPlayer p : Config.getServer().getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(p, new MainPacket(packet));
		}
	} //config

	public static void sendPlayer(BlockMorphPacket packet, ServerPlayer pl) {
		ServerPlayNetworking.send(pl, new MainPacket(packet));
	} //onJoin

	public static void registerPacket(String id, Function<FriendlyByteBuf, BlockMorphPacket> bl, boolean client) {
		ResourceLocation res = ResourceLocation.fromNamespaceAndPath(BlockomorphServer.MOD_ID, id);
		if (handlers.containsKey(res)) {
			throw new IllegalArgumentException("Packet with Id: " + id + " alredy registered!");
		}
		handlers.put(ResourceLocation.fromNamespaceAndPath(BlockomorphServer.MOD_ID, id), new PacketInfo(bl, client));
	}

	public record PacketInfo(Function<FriendlyByteBuf, BlockMorphPacket> packet, boolean isClient) {}

	private static SavedBlockManager getSavedManager() {
		return new SavedBlockManager(FabricLoader.getInstance().getGameDir());
	}

	@Nullable
	public static BannedBlock isBannedBlock(BlockState state, @Nullable Player pl) {
		if (state.getBlock() instanceof LiquidBlock) {
			return new BannedBlock("Morphing in liquids in development!", Component.translatable("commands.blockmorph.liquid"));
		}
		String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		Config.Mode mode = Config.getInstance().getValue("listMode");
		if ((!state.isSolid() || state.getBlock() instanceof BarrierBlock || state.getBlock() instanceof MovingPistonBlock) && (state.getBlock() != Blocks.AIR) && (boolean)Config.getInstance().getValue("solidBlocksOnly")) {
			return new BannedBlock("Block " + name + " not allowed because is solid!", Component.translatable("commands.blockmorph.solid"));
		} else if (pl != null && ((PlayerAccessor)pl).getTnt() != null) {
			return new BannedBlock("Block " + name + " not allowed because player-tnt caught fire!", Component.translatable("commands.blockmorph.tnt"));
		} else if (mode == Config.Mode.WHITELIST) {
			if (!((List<String>)Config.getInstance().getValue("allowedBlocks")).contains(name))
				return new BannedBlock("Block " + name + " not allowed because it not in whitelist!", Component.translatable("commands.blockmorph.whitelist"));
		} else if (mode == Config.Mode.BLACKLIST) {
			if (((List<String>)Config.getInstance().getValue("bannedBlocks")).contains(name))
				return new BannedBlock("Block " + name + " not allowed because it in blacklist!", Component.translatable("commands.blockmorph.blacklist"));
		}
		return null;
	}

	public record BannedBlock(String reason, Component text) {
		public static final BannedBlock SAME = new BannedBlock("You have already been turned into this block.",
				Component.translatable("commands.blockomorph.blockSame"));
	}

	public static boolean onPlayerAttacked(LivingEntity attacked, DamageSource damage, float amount) {
		if (attacked instanceof PlayerAccessor pl) {
			boolean noTnt = pl.getTnt() == null;
			boolean tntBlock = pl.getBlockState(InPlayerBlockPos.ZERO).getBlock() instanceof TntBlock;
			boolean tntDamage =
					damage.is(DamageTypes.PLAYER_EXPLOSION) ||
							damage.is(DamageTypes.EXPLOSION);
			boolean allowed =
					damage.is(DamageTypes.GENERIC_KILL) ||
							damage.is(DamageTypes.FELL_OUT_OF_WORLD);
			if (pl.isActive()) {
				if (allowed || (!tntBlock && tntDamage)) {
					destroy(pl, damage.getEntity());
				} else if (tntBlock && tntDamage && noTnt) {
					pl.setTnt();
					PrimedTnt tnt = pl.getTnt();
					if (tnt != null) {
						tnt.setFuse(tnt.getFuse() / 2);
					}
				}
				return !(damage.is(PLAYER_DESTROYED) || damage.is(PLAYER_DESTROYED_NULL));
			}
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static boolean onHudRender(GuiGraphics GUI) {
		Minecraft mc = Minecraft.getInstance();
		Entity player = mc.getCameraEntity();

		if (player instanceof PlayerAccessor pl && pl.isActive()) {
			int width = mc.getWindow().getGuiScaledWidth();
			int height = mc.getWindow().getGuiScaledHeight();
			renderBlockHeart(GUI, pl, width, height);
			return true;
		}
		return false;
	}

	@Environment(EnvType.CLIENT)
	private static void renderBlockHeart(GuiGraphics gui, PlayerAccessor pl, int width, int height) {
		int maxHearts = 10;
		int progress = pl.getBiggestProgress();

		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = dispatcher.getBlockModel(pl.getBlockState(InPlayerBlockPos.ZERO));
		TextureAtlasSprite sprite = model.getParticleIcon();


		int x = width / 2 - 91;
		int y = height - 39;

		renderBar(gui, x, y, progress);

		for (int i = 0; i < maxHearts; i++) {
			int xPos = x + i * 8;
			int yPos = y;


			if (i < 9 - progress) {
				gui.blitSprite(RenderType::guiTextured, sprite, xPos + 1, yPos + 1, 7, 7);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	private static void renderBar(GuiGraphics graphics, int x, int y, int progress) {
		if (progress == 9) {
			graphics.blit(RenderType::guiTextured, ResourceLocation.fromNamespaceAndPath("blockomorph", "textures/screens/icons.png"), x, y, 0, 10, 81, 9, 81, 19);
		} else {
			graphics.blit(RenderType::guiTextured, ResourceLocation.fromNamespaceAndPath("blockomorph", "textures/screens/icons.png"), x, y, 0, 0, 81, 9, 81, 19);
		}
	}

	public static void destroy(PlayerAccessor mob_pl, @org.jetbrains.annotations.Nullable Entity attacker) {
		Entity mob = (Player)mob_pl;
		if (mob.level() instanceof ServerLevel lv) {
			Holder<DamageType> damage = mob.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).
					getOrThrow(attacker == null ? PLAYER_DESTROYED_NULL : PLAYER_DESTROYED);
			mob.hurtServer(lv, new DamageSource(damage, attacker), Float.MAX_VALUE);
		}
	}
}
