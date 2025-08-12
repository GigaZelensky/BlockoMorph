package net.blockomorph.utils;

import com.mojang.blaze3d.platform.Window;
import net.blockomorph.Blockomorph;
import net.blockomorph.network.*;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.coords.BlockPosBounds;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDrownEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class MorphUtils {
    public static final ResourceKey<DamageType> PLAYER_DESTROYED = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("blockomorph:player_destroyed"));
    public static final ResourceKey<DamageType> PLAYER_DESTROYED_NULL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("blockomorph:player_destroyed_null"));
    public static final SavedBlockManager bmanager = getSavedManager();

    private static SavedBlockManager getSavedManager() {
        return new SavedBlockManager(FMLPaths.GAMEDIR.get().toFile());
    }

    private static final HashMap<ResourceLocation, PacketInfo> handlers = new HashMap<>();

    public static PacketInfo getHandler(ResourceLocation id) {
        return handlers.get(id);
    }

    public static void sendServer(BlockMorphPacket packet) {
        Blockomorph.PACKET_HANDLER.sendToServer(new MainPacket(packet));
    }

    public static void sendAll(BlockMorphPacket packet) {
        Blockomorph.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(), new MainPacket(packet));
    }

    public static void sendPlayer(BlockMorphPacket packet, ServerPlayer pl) {
        Blockomorph.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> pl), new MainPacket(packet));
    }

    public static void registerPacket(String id, Function<FriendlyByteBuf, BlockMorphPacket> bl, boolean client) {
        ResourceLocation res = new ResourceLocation(Blockomorph.MODID, id);
        if (handlers.containsKey(res)) {
            throw new IllegalArgumentException("Packet with Id: " + id + " alredy registered!");
        }
        handlers.put(new ResourceLocation(Blockomorph.MODID, id), new PacketInfo(bl, client));
    }

    public record PacketInfo(Function<FriendlyByteBuf, BlockMorphPacket> packet, boolean isClient) {}

    @SubscribeEvent
    public static void run(ServerStartingEvent event) {
        Config.setServer(event.getServer());
        BlockPosBounds.load();
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        sendPlayer(new ClientBoundConfigUpdatePacket(Config.getInstance()), player);
        PlayerAccessor pl = PlayerAccessor.of(player);
        sendPlayer(new ClientBoundMorphUpdatePacket(pl), player);
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

    public static void executeMorphedBlockShapeUpdate(LevelAccessor level, Direction direction, BlockState state, BlockPos offsetted, BlockPos origin, int flags, int distance, BlockState external) {
        BlockState blockstate1 = external.updateShape(direction, state, level, offsetted, origin);
        Block.updateOrDestroy(external, blockstate1, level, offsetted, flags, distance);
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
            Registry<Block> registry = attacker.level().registryAccess().registryOrThrow(Registries.BLOCK);
            return !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack.hasAdventureModePlaceTagForBlock(registry, blockinworld));
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        Entity attacked = event.getEntity();
        DamageSource damage = event.getSource();
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
                if (!(damage.is(PLAYER_DESTROYED) || damage.is(PLAYER_DESTROYED_NULL))) event.setCanceled(true);
                if (allowed || (!tntBlock && tntDamage)) {
                    destroy(pl, damage.getEntity());
                } else if (tntBlock && tntDamage && noTnt) {
                    pl.setTnt();
                    PrimedTnt tnt = pl.getTnt();
                    if (tnt != null) {
                        tnt.setFuse(tnt.getFuse() / 2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDrown(LivingDrownEvent event) {
        if (event.getEntity() instanceof PlayerAccessor pl) {
            if (pl.isActive()) event.setDrowning(false);
        }
    }

    @Nullable
    public static BannedBlock isBannedBlock(BlockState state, @Nullable Player pl) {
        if (state.getBlock() instanceof LiquidBlock) {
            return new BannedBlock("Morphing in liquids in development!", Component.translatable("commands.blockmorph.liquid"));
        }
        String name = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        Config.Mode mode = Config.getInstance().getValue("listMode");
        if ((!state.isSolid() || state.getBlock() instanceof BarrierBlock || state.getBlock() instanceof MovingPistonBlock) && (state.getBlock() != Blocks.AIR) && (boolean) Config.getInstance().getValue("solidBlocksOnly")) {
            return new BannedBlock("Block " + name + " not allowed because is solid!", Component.translatable("commands.blockmorph.solid"));
        } else if (pl != null && PlayerAccessor.of(pl).getTnt() != null) {
            return new BannedBlock("Block " + name + " not allowed because player-tnt caught fire!", Component.translatable("commands.blockmorph.tnt"));
        } else if (mode == Config.Mode.WHITELIST) {
            if (!((List<String>) Config.getInstance().getValue("allowedBlocks")).contains(name))
                return new BannedBlock("Block " + name + " not allowed because it not in whitelist!", Component.translatable("commands.blockmorph.whitelist"));
        } else if (mode == Config.Mode.BLACKLIST) {
            if (((List<String>) Config.getInstance().getValue("bannedBlocks")).contains(name))
                return new BannedBlock("Block " + name + " not allowed because it in blacklist!", Component.translatable("commands.blockmorph.blacklist"));
        }
        return null;
    }

    public record BannedBlock(String reason, Component text) {
        public static final BannedBlock SAME = new BannedBlock("You have already been turned into this block.",
                Component.translatable("commands.blockomorph.blockSame"));
    }

    public static boolean needRejectUse(Level lv, BlockHitResult block) {
        if (InPlayerBlockPos.isMorphedPlayerX(block.getBlockPos().getX())) {
            BlockState state = lv.getBlockState(block.getBlockPos());
            Config.UseMode mode = Config.getInstance().getValue("useMode");
            switch (mode) {
                case DISABLED -> {
                    return true;
                }
                case VANILLA -> {
                    ResourceLocation res = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    return res != null && !res.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
                }
            }
        }
        return false;
    }

    public static UseOnContext checkOnRealIfOut(UseOnContext ctx, ItemStack stack) {
        if (stack.getItem() instanceof BlockItem && InPlayerBlockPos.isMorphedPlayerX(ctx.getClickedPos().getX())) {
            Config.PlaceMode mode = Config.getInstance().getValue("placeMode");
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
        Vec3i step = dir.getNormal();
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
            Config.PlaceMode mode = Config.getInstance().getValue("placeMode");
            if (mode == Config.PlaceMode.DISABLED && InPlayerBlockPos.isMorphedPlayerX(event.getHitVec().getBlockPos().getX())) {
                event.setUseItem(Event.Result.DENY);
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
    @SubscribeEvent
    public static void onRenderFire(RenderBlockScreenEffectEvent event) {
        if (PlayerAccessor.of(event.getPlayer()).isActive()) {
            if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE) event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onHudRender(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.getCameraEntity();
        ResourceLocation overlayId = event.getOverlay().id();
        boolean flag = mc.gameMode.canHurtPlayer();

        if (player instanceof PlayerAccessor pl && pl.isActive()) {
            if (flag && overlayId.equals(new ResourceLocation("minecraft", "player_health"))) {
                Window w = event.getWindow();
                int width = w.getGuiScaledWidth();
                int height = w.getGuiScaledHeight();
                renderBlockHeart(event.getGuiGraphics(), pl, width, height);
                ((ForgeGui) mc.gui).leftHeight += 10;
                event.setCanceled(true);
            }
            if (overlayId.equals(new ResourceLocation("minecraft", "air_level")))
                event.setCanceled(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
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
                gui.blit(xPos + 1, yPos + 1, 0, 7, 7, sprite);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void renderBar(GuiGraphics graphics, int x, int y, int progress) {
        if (progress == 9) {
            graphics.blit(new ResourceLocation("blockomorph:textures/screens/icons.png"), x, y, 0, 10, 81, 9, 81, 19);
        } else {
            graphics.blit(new ResourceLocation("blockomorph:textures/screens/icons.png"), x, y, 0, 0, 81, 9, 81, 19);
        }
    }

    public static void destroy(PlayerAccessor mob_pl, @Nullable Entity attacker) {
        Entity mob = (Player) mob_pl;

        Collection<TagKey<DamageType>> type = Set.of(
                DamageTypeTags.BYPASSES_INVULNERABILITY,
                DamageTypeTags.BYPASSES_RESISTANCE,
                DamageTypeTags.BYPASSES_EFFECTS,
                DamageTypeTags.BYPASSES_COOLDOWN,
                DamageTypeTags.BYPASSES_ENCHANTMENTS,
                DamageTypeTags.BYPASSES_SHIELD,
                DamageTypeTags.BYPASSES_ARMOR
        );

        Holder.Reference<DamageType> damage = mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).
                getHolderOrThrow(attacker == null ? PLAYER_DESTROYED_NULL : PLAYER_DESTROYED);
        damage.bindTags(type);

        mob.hurt(new DamageSource(damage, attacker), Float.MAX_VALUE);
    }

}
