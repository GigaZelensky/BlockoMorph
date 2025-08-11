package net.blockomorph.utils;

import com.mojang.serialization.DataResult;
import net.blockomorph.Blockomorph;
import net.blockomorph.command.BlockmorphCommand;
import net.blockomorph.command.BlockmorphconfigCommand;
import net.blockomorph.core.KeyMappings;
import net.blockomorph.network.*;
import net.blockomorph.utils.config.*;

import net.blockomorph.utils.coords.BlockPosBounds;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.core.Direction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDrownEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Predicate;


@EventBusSubscriber
public class MorphUtils {
	public static final ResourceKey<DamageType> PLAYER_DESTROYED = ResourceKey.create(Registries.DAMAGE_TYPE, res("player_destroyed"));
	public static final ResourceKey<DamageType> PLAYER_DESTROYED_NULL = ResourceKey.create(Registries.DAMAGE_TYPE, res("player_destroyed_null"));
	public static final Logger LOGGER = LoggerFactory.getLogger(Blockomorph.MODID);
	public static Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(Blockomorph.MODID, path);
	}

	public static ResourceLocation vanillaRes(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}


	/****************************PACKET SYSTEM************************************/

	private static final HashMap<ResourceLocation, PacketInfo> handlers = new HashMap<>();
	public static PacketInfo getHandler(ResourceLocation id) {
		return handlers.get(id);
	}

	public static void sendServer(BlockMorphPacket packet) {
		PacketDistributor.sendToServer(new MainPacket(packet));
	}

	public static void sendAll(BlockMorphPacket packet) {
		PacketDistributor.sendToAllPlayers(new MainPacket(packet));
	}

	public static void sendPlayer(BlockMorphPacket packet, ServerPlayer pl) {
		PacketDistributor.sendToPlayer(pl, new MainPacket(packet));
	}

	public static void registerPacket(String id, Function<FriendlyByteBuf, BlockMorphPacket> bl, boolean client) {
		ResourceLocation res = ResourceLocation.fromNamespaceAndPath(Blockomorph.MODID, id);
		if (handlers.containsKey(res)) {
			throw new IllegalArgumentException("Packet with Id: " + id + " alredy registered!");
		}
		handlers.put(ResourceLocation.fromNamespaceAndPath(Blockomorph.MODID, id), new PacketInfo(bl, client));
	}

	public record PacketInfo(Function<FriendlyByteBuf, BlockMorphPacket> packet, boolean isClient) {}

	/****************************PACKET SYSTEM************************************/

	@SubscribeEvent
	public static void run(ServerStartingEvent event) {
		Config.setServer(event.getServer());
		BlockPosBounds.load();
	}

	@SubscribeEvent
	public static void commandRegister(RegisterCommandsEvent event) {
		BlockmorphconfigCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
		BlockmorphCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
	}

	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
	public static class ModBus {
		@SubscribeEvent
		public static void registerKeys(RegisterKeyMappingsEvent event) {
			KeyMappings.registerKeyMappings(event::register);
		}
	}

	@SubscribeEvent
	public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		sendPlayer(new ClientBoundConfigUpdatePacket(Config.getInstance()), player);
		PlayerAccessor pl = PlayerAccessor.of(player);
		sendPlayer(new ClientBoundMorphUpdatePacket(pl), player);
	}

	public static Predicate<String> blockPredicate() {
		return value -> {
			DataResult<ResourceLocation> result = ResourceLocation.read(value);
			if (result.result().isPresent()) {
				return BuiltInRegistries.BLOCK.containsKey(result.result().get());
			}
			return false;
		};
	}

	public static Vec3 getRealBlockPos(PlayerAccessor original, InPlayerBlockPos offset) {
		return getRealBlockPos(original, new Vec3(offset.x, offset.y, offset.z));
	}

	public static Vec3 getRealBlockPos(PlayerAccessor original, Vec3 offset) {
		AABB aabb = original.player().getBoundingBox();
		InPlayerBlockPos minPos = original.minPos();

		double deltaX = offset.x - (double) minPos.getX();
		double deltaY = offset.y - (double) minPos.getY();
		double deltaZ = offset.z - (double) minPos.getZ();

		double globalX = aabb.minX + deltaX;
		double globalY = aabb.minY + deltaY;
		double globalZ = aabb.minZ + deltaZ;

		return new Vec3(globalX, globalY, globalZ);
	}

	public static void distanceTo(Vec3 from, Vec3 to, boolean sqr, double offset, DoubleConsumer action) {
		if (InPlayerBlockPos.isMorphedPlayerX(from.x) || InPlayerBlockPos.isMorphedPlayerX(to.x) && action != null) {
			from = InPlayerBlockPos.checkOnReal(from);
			to = InPlayerBlockPos.checkOnReal(to);
			double d0 = from.x + offset - to.x;
			double d1 = from.y + offset - to.y;
			double d2 = from.z + offset - to.z;
			double result = d0 * d0 + d1 * d1 + d2 * d2;
			if (!sqr)
				result = Math.sqrt(result);
			action.accept(result);
		}
	}

	public static void executeMorphedBlockShapeUpdate(LevelAccessor levelAccessor, Direction direction, BlockPos blockPos, BlockPos blockPos2, BlockState blockState, int i, int j, BlockState external) {
		if ((i & 128) == 0 || !external.is(Blocks.REDSTONE_WIRE)) {
			BlockState blockState3 = external.updateShape(levelAccessor, levelAccessor, blockPos, direction, blockPos2, blockState, levelAccessor.getRandom());
			Block.updateOrDestroy(external, blockState3, levelAccessor, blockPos, i, j);
		}
	}

	public static Vec3 getCetneredRealBlockPos(PlayerAccessor original, InPlayerBlockPos offset) {
		Vec3 vec = getRealBlockPos(original, offset);
		return new Vec3(vec.x + 0.5, vec.y + 0.5, vec.z + 0.5);
	}

	public static boolean isAdventureCanBreak(PlayerAccessor pl, Player attacker, InPlayerBlockPos hitPart) {
		BlockInPlayer2 block = pl.getBlocksData2().get(hitPart);
		if (block != null) {
			BlockInWorld blockinworld = new BlockInWorld(pl.player().level(), block.getPos(), true);
			ItemStack itemstack = attacker.getMainHandItem();
			return !itemstack.isEmpty() && (itemstack.canBreakBlockInAdventureMode(blockinworld) || itemstack.canPlaceOnBlockInAdventureMode(blockinworld));
		}
		return false;
	}

	public static boolean needRejectUse(Level lv, BlockHitResult block) {
		if (InPlayerBlockPos.isMorphedPlayerX(block.getBlockPos().getX())) {
			BlockState state = lv.getBlockState(block.getBlockPos());
			Config.UseMode mode = Config.getInstance().getValue("useMode", Config.UseMode.class);
			switch (mode) {
				case DISABLED -> {
					return true;
				}
				case VANILLA -> {
					ResourceLocation res = BuiltInRegistries.BLOCK.getKey(state.getBlock());
					return !res.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
				}
			}
		}
		return false;
	}

	public static UseOnContext checkOnRealIfOut(UseOnContext ctx, ItemStack stack) {
		if (stack.getItem() instanceof BlockItem && InPlayerBlockPos.isMorphedPlayerX(ctx.getClickedPos().getX())) {
			Config.PlaceMode mode = Config.getInstance().getValue("placeMode", Config.PlaceMode.class);
			if (mode == Config.PlaceMode.OUT) {
				Vec3 realHit = InPlayerBlockPos.checkOnReal(ctx.getClickLocation());
				realHit = toDirection(realHit, ctx.getClickedFace());
				BlockHitResult hit = new BlockHitResult(realHit, ctx.getClickedFace(), BlockPos.containing(realHit), ctx.isInside());
				return new UseOnContext(ctx.getLevel(), ctx.getPlayer(), ctx.getHand(), ctx.getItemInHand(), hit);
			}
		}
		return ctx;
	}

	private static Vec3 toDirection(Vec3 vec, Direction dir) {
		Vec3i step = dir.getUnitVec3i();
		double x = switch (step.getX()) {
			case 1 -> Math.ceil(vec.x) + 1.0E-7;
			case -1 -> Math.floor(vec.x) - 1.0E-7;
			default -> vec.x;
		};
		double y = switch (step.getY()) {
			case 1 -> Math.ceil(vec.y) + 1.0E-7;
			case -1 -> Math.floor(vec.y) - 1.0E-7;
			default -> vec.y;
		};
		double z = switch (step.getZ()) {
			case 1 -> Math.ceil(vec.z) + 1.0E-7;
			case -1 -> Math.floor(vec.z) - 1.0E-7;
			default -> vec.z;
		};
		return new Vec3(x, y, z);
	}

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity().getItemInHand(event.getHand()).getItem() instanceof BlockItem) {
			Config.PlaceMode mode = Config.getInstance().getValue("placeMode", Config.PlaceMode.class);
			if (mode == Config.PlaceMode.DISABLED && InPlayerBlockPos.isMorphedPlayerX(event.getHitVec().getBlockPos().getX())) {
				event.setUseItem(TriState.FALSE);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean canOpenMenuIn(PlayerAccessor pl, InPlayerBlockPos offset) {
		BlockInPlayer2 block = pl.getBlocksData2().get(offset);
		if (block != null) {
			MenuProvider pr = block.getBlockState().getMenuProvider(pl.player().level(), block.getPos());
			return pr != null;
		}
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean canOpenConfig() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player != null && mc.player.hasPermissions(2) && Config.getInstance().getValue("canOperatorModifyConfig", Boolean.class);
	}

	public static Config.ScreenAccess getScreenAccess(Player player) {
		if (player != null && player.getPermissionLevel() > 2) return Config.ScreenAccess.ALL;
		return Config.getInstance().getValue("screenAccess", Config.ScreenAccess.class);
	}

	public static boolean onPlayerAttacked(DamageSource damage, Entity attacked) {
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

	@SubscribeEvent
	public static void onPlayerDrown(LivingDrownEvent event) {
		if (event.getEntity() instanceof PlayerAccessor pl) {
			if (pl.isActive()) event.setDrowning(false);
		}
	}

	public static void destroy(PlayerAccessor mob_pl, @Nullable Entity attacker) {
		LivingEntity mob = (Player)mob_pl;
		if (mob.level() instanceof ServerLevel lv) {

			Holder<DamageType> damage = mob.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).
					getOrThrow(attacker == null ? PLAYER_DESTROYED_NULL : PLAYER_DESTROYED);
			mob.hurtServer(lv, new DamageSource(damage, attacker), Float.MAX_VALUE);

		}
	}
}
