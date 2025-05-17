package net.blockomorph.utils;

import com.mojang.blaze3d.platform.Window;
import net.blockomorph.Blockomorph;
import net.blockomorph.network.*;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.accessors.GamemodeAccessor;
import net.blockomorph.utils.config.Config;
import net.blockomorph.utils.hit.MorphedPlayerHitResult;
import net.blockomorph.utils.hit.PlayerHitResult;
import net.blockomorph.utils.use.UseController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDrownEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class MorphUtils {
    public static final ResourceKey<DamageType> PLAYER_DESTROYED = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("blockomorph:player_destroyed"));
    public static final ResourceKey<DamageType> PLAYER_DESTROYED_NULL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("blockomorph:player_destroyed_null"));
    public static final SavedBlockManager bmanager = getSavedManager();
    private static boolean attackPressed;
    //clientside
    public static MorphedPlayerHitResult playerHitResult;
    public static PlayerAccessor hitEntity;
    public static BlockPos hitPart;
    public static Vec3 hitLocation;
    public static Vec3 inBlockHitOffset;
    public static Direction hitDirection;

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

    public record PacketInfo(Function<FriendlyByteBuf, BlockMorphPacket> packet, boolean isClient) {
    }

    public static BlockPos parseBlockPos(String input) {
        String[] parts = input.trim().split(" ");

        if (parts.length != 3) return null;

        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);

            return new BlockPos(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean doActionFromBounedBlockPos(BlockPos pos, BiConsumer<UseController, BlockPos> action) {
        if (pos == null || action == null) return false;
        UseController ctr = BlockPosAccessor.of(pos).getController();
        if (ctr != null) {
            action.accept(ctr, ctr.getOffset());
            return true;
        }
        return false;
    }

    @Nullable
    public static UseController getControllerFromNetwork(UUID id, BlockPos offset) {
        MinecraftServer sv = Config.getServer();
        PlayerAccessor pl = null;
        if (sv != null) {
            for (ServerLevel lv : sv.getAllLevels()) {
                if (lv.getEntity(id) instanceof PlayerAccessor playerAccessor) {
                    pl = playerAccessor;
                    break;
                }
            }
        } else {
            for (Entity ent : Minecraft.getInstance().level.entitiesForRendering()) {
                if (ent.getUUID().equals(id)) {
                    if (ent instanceof PlayerAccessor playerAccessor) {
                        pl = playerAccessor;
                        break;
                    }
                }
            }
        }
        if (pl != null) {
            return pl.getUseControllers().get(offset);
        }
        return null;
    }

    public static String getBlockPos(BlockPos offset) {
        return offset.getX() + " " + offset.getY() + " " + offset.getZ();
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag originalNBT = event.getOriginal().saveWithoutId(new CompoundTag());

        if (originalNBT.contains("BlockoMorph")) {
            CompoundTag tag = new CompoundTag();
            tag.put("BlockoMorph", originalNBT.getCompound("BlockoMorph"));
            event.getEntity().load(tag);
        }
    }

    @SubscribeEvent
    public static void run(ServerStartingEvent event) {
        Config.setServer(event.getServer());
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        sendPlayer(new ClientBoundConfigUpdatePacket(Config.getInstance()), (ServerPlayer) event.getEntity());
    }

    public static Vec3 getRealBlockPos(PlayerAccessor original, BlockPos offset) {
        AABB aabb = original.player().getBoundingBox();
        BlockPos minPos = original.minPos();

        int deltaX = offset.getX() - minPos.getX();
        int deltaY = offset.getY() - minPos.getY();
        int deltaZ = offset.getZ() - minPos.getZ();

        double globalX = aabb.minX + deltaX;
        double globalY = aabb.minY + deltaY;
        double globalZ = aabb.minZ + deltaZ;

        return new Vec3(globalX, globalY, globalZ);
    }

    public static Vec3 getCetneredRealBlockPos(PlayerAccessor original, BlockPos offset) {
        Vec3 vec = getRealBlockPos(original, offset);
        return new Vec3(vec.x + 0.5, vec.y + 0.5, vec.z + 0.5);
    }

    public static boolean onPlayerAttack(Player player, Entity mob, BlockPos part) {
        if (mob.level().isClientSide() && mob instanceof PlayerAccessor mb2) mb2.setReady(false);
        if (mob instanceof PlayerAccessor mb && player instanceof ServerPlayer pl && mb.isFullActive() && part != null) {
            GameType gm = pl.gameMode.getGameModeForPlayer();
            if (player.isCreative()) {
                destroy(mb, player);
                player.swing(InteractionHand.MAIN_HAND, true);
            } else if (gm == GameType.SURVIVAL) {
                mb.addPlayer(part, player);
            } else if (gm == GameType.ADVENTURE) {
                if (isAdventureCanBreak(mb, pl, part))
                    mb.addPlayer(part, player);
            }
            return true;
        }
        return false;
    }

    public static boolean isAdventureCanBreak(PlayerAccessor pl, Player attacker, BlockPos hitPart) {
        UseController ctr = pl.getUseControllers().get(hitPart);
        BlockInWorld blockinworld = new BlockInWorld(ctr.getUseLevel(), ctr.getOffset(), true);
        ItemStack itemstack = attacker.getMainHandItem();
        Registry<Block> registry = attacker.level().registryAccess().registryOrThrow(Registries.BLOCK);
        return !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack.hasAdventureModePlaceTagForBlock(registry, blockinworld));
    }

    @SubscribeEvent
    public static void onPlayerAttacked(LivingAttackEvent event) {
        Entity attacked = event.getEntity();
        DamageSource damage = event.getSource();
        if (attacked instanceof PlayerAccessor pl) {
            boolean noTnt = pl.getTnt() == null;
            boolean tntBlock = pl.getBlockState().getBlock() instanceof TntBlock;
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.phase == TickEvent.Phase.END) {
            boolean isAttackPressed = mc.options.keyAttack.isDown();

            if (playerHitResult != null && hitEntity.isFullActive()) {
                int i = hitEntity.getBiggestProgress();
                if ((hitPart != null && !isAttackPressed && i > -1) || (attackPressed && !isAttackPressed)) {
                    MorphUtils.sendServer(new ServerBoundInteractBlockPacket(false, -1, hitPart));
                    hitEntity.setReady(true);
                }
                if (i > -1 && isAttackPressed && hitPart != null) {
                    Vec3 realPos = getRealBlockPos(hitEntity, hitPart);
                    VoxelShape shape = hitEntity.getRenderShape(hitPart).move(-hitPart.getX(), -hitPart.getY(), -hitPart.getZ()).move(realPos.x, realPos.y, realPos.z);
                    crackBlock(hitEntity, hitDirection, shape, hitEntity.getBlocks().get(hitPart));
                }
                if (isAttackPressed && hitEntity.readyForDestroy()) {
                    GamemodeAccessor gm = ((GamemodeAccessor) mc.gameMode);
                    if (gm.getDelay() > 0) {
                        gm.setDelay(gm.getDelay() - 1);
                    } else {
                        MorphUtils.sendServer(new ServerBoundInteractBlockPacket(true, hitEntity.player().getId(), hitPart));
                        onPlayerAttack(mc.player, (Player)hitEntity, null);
                    }
                }
            }
            attackPressed = isAttackPressed;

        }
    }

    @Nullable
    public static BannedBlock isBannedBlock(BlockState state, @Nullable Player pl) {
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
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean canOpenMenuIn(PlayerAccessor pl, BlockPos offset) {
        UseController block = pl.getUseControllers().get(offset);
        if (block != null) {
            MenuProvider pr = block.getBlockState().getMenuProvider(block.getUseLevel(), block.getOffset());
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
    public static void onAttackBlockPlayer(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (playerHitResult != null && hitEntity.isFullActive()) {
            if (event.isAttack()) {
                event.setCanceled(true);
                if (hitEntity instanceof Player) {
                    MorphUtils.sendServer(new ServerBoundInteractBlockPacket(true, hitEntity.player().getId(), hitPart));
                    onPlayerAttack(mc.player, (Player)hitEntity, null);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean performClientUse(PlayerAccessor pl, InteractionHand interactionhand) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ItemStack itemstack = player.getItemInHand(interactionhand);

        int i = itemstack.getCount();
        BlockHitResult hitResult = new BlockHitResult(playerHitResult.getInBlockOffset(), playerHitResult.getDirection(), playerHitResult.getOffset(), playerHitResult.isInside());

        Blockomorph.LOGGER.info("client!");

        InteractionResult interactionresult1 = pl.clickPlayer(player, hitResult, interactionhand);
        sendServer(new ServerBoundUseBlockPacket(hitResult, pl.player().getId(), interactionhand));
        if (interactionresult1.consumesAction()) {
            if (interactionresult1.shouldSwing()) {
                player.swing(interactionhand);
                if (!itemstack.isEmpty() && (itemstack.getCount() != i || mc.gameMode.hasInfiniteItems())) {
                    mc.gameRenderer.itemInHandRenderer.itemUsed(interactionhand);
                }
            }
            return true;
        }
        return interactionresult1 == InteractionResult.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onPick(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        boolean isAttackPressed = mc.options.keyAttack.isDown();
        if (mc.getCameraEntity() instanceof Player pl) {
            MorphedPlayerHitResult res = PlayerHitResult.calculateMorphedPlayerHitResult(pl, -1, 1, ClipContext.Block.OUTLINE, false);
            PlayerAccessor old = hitEntity;
            if (res != null) {
                hitEntity = res.getPlayer();
                hitPart = res.getOffset();
                hitLocation = res.getLocation();
                inBlockHitOffset = res.getInBlockOffset();
                hitDirection = res.getDirection();
                playerHitResult = res;
            } else {
                hitEntity = null;
                hitPart = null;
                hitLocation = null;
                inBlockHitOffset = null;
                hitDirection = null;
                playerHitResult = null;
            }

            if (old != hitEntity) {
                if (hitEntity != null) {
                    hitEntity.setReady(true);
                } else if (isAttackPressed) {
                    ((GamemodeAccessor) mc.gameMode).setDelay(5);
                }
            }

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
        BakedModel model = dispatcher.getBlockModel(pl.getBlockState());
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
        HashMap<BlockPos, BlockState> blocks = new HashMap<>(mob_pl.getBlocks());
        blocks.put(new BlockPos(0, 0, 0), mob_pl.getBlockState());
        boolean hasTnt = mob_pl.getTnt() != null;

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

        if (Config.getInstance().getValue("playerDieAfterDestroy")) {
            mob.hurt(new DamageSource(damage, attacker), Float.MAX_VALUE);
        } else {
            mob_pl.applyBlockMorph(Blocks.AIR.defaultBlockState(), new CompoundTag());
        }

        if (mob.level() instanceof ServerLevel lv && !hasTnt) {
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState val = entry.getValue();
                VoxelShape shape2 = val.getCollisionShape(lv, mob.blockPosition(), CollisionContext.of(mob));
                //particle(lv, mob.getX() + pos.getX(), mob.getY() + pos.getY(), mob.getZ() + pos.getZ(), val, shape2);
                particle(lv, getRealBlockPos(mob_pl, pos), val, shape2);
                SoundType soundtype = val.getSoundType();
                lv.playSound(null, mob.blockPosition().offset(pos), soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            }
        }
    }

    private static Direction calculateHitDirection(Vec3 hitVec, AABB boundingBox) {
        double xDist = Math.min(Math.abs(hitVec.x - boundingBox.minX), Math.abs(hitVec.x - boundingBox.maxX));
        double yDist = Math.min(Math.abs(hitVec.y - boundingBox.minY), Math.abs(hitVec.y - boundingBox.maxY));
        double zDist = Math.min(Math.abs(hitVec.z - boundingBox.minZ), Math.abs(hitVec.z - boundingBox.maxZ));

        if (xDist < yDist && xDist < zDist) {
            return hitVec.x < boundingBox.getCenter().x ? Direction.WEST : Direction.EAST;
        } else if (yDist < xDist && yDist < zDist) {
            return hitVec.y < boundingBox.getCenter().y ? Direction.DOWN : Direction.UP;
        } else {
            return hitVec.z < boundingBox.getCenter().z ? Direction.NORTH : Direction.SOUTH;
        }
    }

    @Nullable
    public static Direction getClosestHitSide(VoxelShape voxelShape, Vec3 hitPosition) {
        for (AABB aabb : voxelShape.toAabbs()) {
            if (containsAABB(aabb, hitPosition, 1.0E-7)) {
                return calculateHitDirection(hitPosition, aabb);
            }
        }

        return null;
    }

    private static boolean containsAABB(AABB ab, Vec3 tr, double tolerance) {
        double d = tr.x;
        double e = tr.y;
        double f = tr.z;
        return (d >= ab.minX - tolerance && d <= ab.maxX + tolerance) &&
                (e >= ab.minY - tolerance && e <= ab.maxY + tolerance) &&
                (f >= ab.minZ - tolerance && f <= ab.maxZ + tolerance);
    }

    protected static void particle(ServerLevel world, Vec3 startPos, BlockState blockState, VoxelShape shape) {
        double x = startPos.x();
        double y = startPos.y();
        double z = startPos.z();
        if (!blockState.isAir()) {
            double d0 = 0.25D;

            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double d1 = Math.min(1.0D, maxX - minX);
                double d2 = Math.min(1.0D, maxY - minY);
                double d3 = Math.min(1.0D, maxZ - minZ);
                int i = Math.max(2, Mth.ceil(d1 / d0));
                int j = Math.max(2, Mth.ceil(d2 / d0));
                int k = Math.max(2, Mth.ceil(d3 / d0));

                for (int l = 0; l < i; ++l) {
                    for (int i1 = 0; i1 < j; ++i1) {
                        for (int j1 = 0; j1 < k; ++j1) {
                            double d4 = ((double) l + 0.5D) / (double) i;
                            double d5 = ((double) i1 + 0.5D) / (double) j;
                            double d6 = ((double) j1 + 0.5D) / (double) k;
                            double particleX = d4 * d1;
                            double particleY = d5 * d2;
                            double particleZ = d6 * d3;

                            BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
                            world.sendParticles(
                                    particleData,
                                    x + particleX - 0.5D, y + particleY, z + particleZ - 0.5D,
                                    1,
                                    d4 - 0.5D, d5 - 0.5D, d6 - 0.5D,
                                    0.25D
                            );
                        }
                    }
                }
            });
        }
    }
    /*protected static void particle(ServerLevel world, BlockState blockState, VoxelShape shape) {
        if (!blockState.isAir()) {
            double d0 = 0.25D;

            shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double d1 = Math.min(1.0D, maxX - minX);
                double d2 = Math.min(1.0D, maxY - minY);
                double d3 = Math.min(1.0D, maxZ - minZ);
                int i = Math.max(2, Mth.ceil(d1 / d0));
                int j = Math.max(2, Mth.ceil(d2 / d0));
                int k = Math.max(2, Mth.ceil(d3 / d0));

                for (int l = 0; l < i; ++l) {
                    for (int i1 = 0; i1 < j; ++i1) {
                        for (int j1 = 0; j1 < k; ++j1) {
                            double d4 = ((double) l + 0.5D) / (double) i;
                            double d5 = ((double) i1 + 0.5D) / (double) j;
                            double d6 = ((double) j1 + 0.5D) / (double) k;
                            double particleX = minX + d4 * d1;  // MinX
                            double particleY = minY + d5 * d2;  // MinY
                            double particleZ = minZ + d6 * d3;  // MinZ

                            BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockState);
                            world.sendParticles(
                                    particleData,
                                    particleX, particleY, particleZ, // Теперь particleX, particleY, particleZ - мировые координаты
                                    1,
                                    d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, // Нет смещения
                                    0.25D
                            );
                        }
                    }
                }
            });
        }
    }*/

    @OnlyIn(Dist.CLIENT)
    public static void crackBlock(PlayerAccessor player, Direction dir, VoxelShape shape, BlockState blockstate) {
        Entity pl = (Entity) player;
        Level level = pl.level();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE && level instanceof ClientLevel lv && !shape.isEmpty()) {

            AABB aabb = shape.bounds();

            float f = 0.1F;
            RandomSource random = RandomSource.create();

            double d0 = aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX - (double) 0.2F) + f;
            double d1 = aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY - (double) 0.2F) + f;
            double d2 = aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ - (double) 0.2F) + f;

            if (dir == Direction.DOWN) {
                d1 = aabb.minY - f;
            }

            if (dir == Direction.UP) {
                d1 = aabb.maxY + f;
            }

            if (dir == Direction.NORTH) {
                d2 = aabb.minZ - f;
            }

            if (dir == Direction.SOUTH) {
                d2 = aabb.maxZ + f;
            }

            if (dir == Direction.WEST) {
                d0 = aabb.minX - f;
            }

            if (dir == Direction.EAST) {
                d0 = aabb.maxX + f;
            }
            Minecraft.getInstance().particleEngine.add((new TerrainParticle(lv, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate)).setPower(0.2F).scale(0.6F));
        }
    }

    private record Hit(
            Entity closestEntity,
            double closestDistance,
            Optional<Vec3> result,
            EntityHitResult hit2,
            BlockPos hitP
    ) {
    }


    //DEBUG DO NOT DELETE!!!!!!!!!!!!!!!!!!!!!!!!!!


   /*public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
		BlockPos pos = BlockPos.ZERO.offset(1, 0, -1);
		for (int x = 0; x < 3; x++) { // Увеличиваем по оси X
			for (int z = 0; z < 3; z++) { // Увеличиваем по оси Z
				BlockPos blockPos2 = pos.offset(x, 0, z); // Смещаем позицию
				BlockState blockState2 = Blocks.STRIPPED_OAK_WOOD.defaultBlockState(); // Устанавливаем блок акации
				level.setBlock(blockPos2, blockState2, 3); // Устанавливаем блок
			}
		}
		BlockPos p2 = pos.offset(3, 0, 0);
		for (int z = 0; z < 3; z++) {
			level.setBlock(p2.offset(0, 0, z), Blocks.OAK_STAIRS.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.WEST), 3);
		}
		BlockPos p3 = pos.offset(0, 1, 0);
		for (int y = 0; y < 3; y++) {
			level.setBlock(p3.offset(0, y, 0), Blocks.SPRUCE_LOG.defaultBlockState(), 3);
		}
		p3 = p3.offset(0, 0, 2);
		for (int y = 0; y < 3; y++) {
			level.setBlock(p3.offset(0, y, 0), Blocks.SPRUCE_LOG.defaultBlockState(), 3);
		}
		for (int y = 0; y < 3; y++) {
			level.setBlock(p3.offset(0, y, -1), Blocks.COMPOSTER.defaultBlockState(), 3);
		}
		for (int x = 0; x < 2; x++) { // Увеличиваем по оси X
			for (int z = 0; z < 3; z++) { // Увеличиваем по оси Z
				BlockPos blockPos2 = pos.offset(x, 4, z); // Смещаем позицию
				BlockState blockState2 = Blocks.OAK_SLAB.defaultBlockState(); // Устанавливаем блок акации
				level.setBlock(blockPos2, blockState2, 3); // Устанавливаем блок
			}
		}
		level.setBlock(pos.offset(1, 1, 1), Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, 2), 3);
		level.setBlock(pos.offset(1, 3, 1), Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true), 3);
		BlockPos p4 = pos.offset(1, 1, 0);
		BlockState st = Blocks.OAK_FENCE.defaultBlockState().setValue(PipeBlock.WEST, true);
		for (int y = 0; y < 3; y++) {
			level.setBlock(p4.offset(0, y, 0), st, 3);
		}
		for (int y = 0; y < 3; y++) {
			level.setBlock(p4.offset(0, y, 2), st, 3);
		}
	}*/

}
