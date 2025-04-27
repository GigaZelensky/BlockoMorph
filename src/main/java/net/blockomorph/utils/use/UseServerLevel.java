package net.blockomorph.utils.use;


import net.blockomorph.network.blockFix.ClientBoundBlockEventPacket;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.EntityAccessor;
import net.blockomorph.utils.accessors.ServerLevelAccessor;
import net.blockomorph.utils.use.fix.UseServerLevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTicks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

public class UseServerLevel extends ServerLevel implements UseAccessor {
    private final HashMap<BlockPos, BlockState> blocks = new HashMap<>();
    protected ServerLevel realLevel;
    protected final UseController useController;
    private boolean realBlockPosMode;

    UseServerLevel(ServerLevel real, UseController ctr) {
        super(
                real.getServer(),
                (runnale) -> {},
                (LevelStorageSource.LevelStorageAccess) ((ServerLevelAccessor)real).getInitArgs().get(2),
                new UseServerLevelData(real),
                real.dimension(),
                (LevelStem) ((ServerLevelAccessor)real).getInitArgs().get(1),
                getChunkListener(),
                false,
                0L,
                List.of(),
                false,
                getRandomS()
        );
        this.realLevel = Objects.requireNonNull(real);
        this.useController = ctr;
    }

    public ServerLevel getRealLevel() {
        return this.realLevel;
    }


    //use level \/

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
        this.blocks.put(this.calculateRealPos(blockPos), blockState);
        return true;
    }

    public HashMap<BlockPos, BlockState> getBlocks() {
        return this.blocks;
    }

    @Override
    public boolean isRealPosMode() {
        return this.realBlockPosMode;
    }

    @Override
    public UseController getController() {
        return this.useController;
    }


    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos bl) {
        BlockPos pos = this.calculateRealPos(bl);
        UseController ctr = this.useController.getPl().getUseControllers().get(pos);
        if (ctr == null) return null;
        return ctr.getBlockEntity();
    }

    public void setRealBlockPosMode(boolean b) {
        this.realBlockPosMode = b;
    }

    @Override
    public List<ServerPlayer> players() {
        ArrayList<ServerPlayer> pls = new ArrayList<>(realLevel.players());
        pls.remove((ServerPlayer) this.useController.getOwner());
        return pls;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        BlockPos pos = this.calculateRealPos(blockPos);
        if (this.realBlockPosMode) {
            if (pos.equals(this.useController.getOffset())) {
                return this.useController.getBlockState();
            } else {
                return realLevel.getBlockState(blockPos);
            }
        }
        BlockState st = this.useController.getPl().getBlocks().get(pos);
        if (st == null) return Blocks.AIR.defaultBlockState();
        return st;
    }

    @Override
    public void blockEntityChanged(BlockPos p_151544_) {
        this.useController.checkChanges();
    }

    @Override
    public boolean addFreshEntity(Entity ent) {
        ((EntityAccessor)ent).forceLevelChange(realLevel);
        return realLevel.addFreshEntity(ent);
    }

    public void playSound(@Nullable Player pl, BlockPos p_46561_, SoundEvent p_46562_, SoundSource p_46563_, float p_46564_, float p_46565_) {
        Player owner = this.useController.getOwner();
        p_46561_ = this.calculateRealPos(p_46561_);
        realLevel.playSound(pl,
                (double)p_46561_.getX() + 0.5D + owner.getX(),
                (double)p_46561_.getY() + 0.5D + owner.getY(),
                (double)p_46561_.getZ() + 0.5D + owner.getZ(),
                p_46562_, p_46563_, p_46564_, p_46565_);
    }

    public void playLocalSound(BlockPos p_250938_, SoundEvent p_252209_, SoundSource p_249161_, float p_249980_, float p_250277_, boolean p_250151_) {
        Player owner = this.useController.getOwner();
        p_250938_ = this.calculateRealPos(p_250938_);
        realLevel.playLocalSound(
                (double)p_250938_.getX() + 0.5D + owner.getX(),
                (double)p_250938_.getY() + 0.5D + owner.getY(),
                (double)p_250938_.getZ() + 0.5D + owner.getZ(),
                p_252209_, p_249161_, p_249980_, p_250277_, p_250151_);
    }

    public void playSeededSound(@Nullable Player var1, double x, double y, double z, Holder<SoundEvent> var8, SoundSource var9, float var10, float var11, long var12) {
        Player owner = this.useController.getOwner();
        if (!this.realBlockPosMode && !(BlockPos.containing(x, y, z).equals(BlockPos.containing(this.useController.getRealPos())))) {
            x = x + owner.getX();
            y = y + owner.getY();
            z = z + owner.getZ();
        }
        //System.out.println(new Vec3(x, y, z));
        realLevel.playSeededSound(var1, x, y, z, var8, var9, var10, var11, var12);
    }

    public void blockEvent(BlockPos blockPos, Block block, int a, int b) {
        if (this.useController.getBlockState().triggerEvent(this, this.useController.getOffset(), a, b)) {
            MorphUtils.sendAll(ClientBoundBlockEventPacket.blockEvent(this.useController.getOwner().getId(), this.useController.getOffset(), a, b));
        }
    }

    public void levelEvent(@Nullable Player player, int a, BlockPos blockPos, int b) {
        if (a == 1010) a = -2;
        if (a == 1011) a = -3;
        ClientBoundBlockEventPacket packet = ClientBoundBlockEventPacket.levelEvent(this.useController.getOwner().getId(), this.useController.getOffset(), a, b);
        if (player == null) {
            MorphUtils.sendAll(packet);
        } else if (player instanceof ServerPlayer pl){
            MorphUtils.sendPlayer(packet, pl);
        }
    }

    @Override
    public boolean noCollision(AABB input) {
        if (input.getCenter().distanceToSqr(this.useController.getOffset().getCenter()) < 64) {
            input = input.move(this.useController.getRealPos().add(-0.5, -0.5, -0.5));
        }
        return realLevel.noCollision(input);
    }

    //use level /\













    //constructor

    protected void initCapabilities() {}

    private static RandomSequences getRandomS() {
        return new RandomSequences(0L) {
            @Override
            public @NotNull RandomSource get(@NotNull ResourceLocation res) {
                return RandomSource.create();
            }

            @Override
            public void save(@NotNull File ignored) {}

            @Override
            public @NotNull CompoundTag save(@NotNull CompoundTag input) {
                return input;
            }
        };
    }

    private static ChunkProgressListener getChunkListener() {
        return new ChunkProgressListener() {
            @Override
            public void updateSpawnPos(@NotNull ChunkPos p_9617_) {}//setworldspawn

            @Override
            public void onStatusChange(@NotNull ChunkPos p_9618_, @Nullable ChunkStatus p_9619_) {}//chunk state update(loading, ready, etc)

            @Override
            public void start() {} //chunk loading start

            @Override
            public void stop() {}//chunk unload
        };
    }

    //suppliers

    public void updateNeighborsAt(BlockPos blockPos, Block p_215046_) {
        //this.neighborUpdater.updateNeighborsAtExceptFromFacing(p_215045_, p_215046_, (Direction)null);
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block p_215053_, Direction p_215054_) {
        //java.util.EnumSet<Direction> directions = java.util.EnumSet.allOf(Direction.class);
        //directions.remove(p_215054_);
        //this.neighborUpdater.updateNeighborsAtExceptFromFacing(p_215052_, p_215053_, p_215054_);
    }

    public void neighborChanged(BlockPos blockPos, Block p_215049_, BlockPos p_215050_) {
        //this.neighborUpdater.neighborChanged(p_215048_, p_215049_, p_215050_);
    }

    public void neighborChanged(BlockState p_215035_, BlockPos blockPos, Block p_215037_, BlockPos p_215038_, boolean p_215039_) {
        //this.neighborUpdater.neighborChanged(p_215035_, p_215036_, p_215037_, p_215038_, p_215039_);
    }

    public @NotNull MinecraftServer getServer() {
        return realLevel.getServer();
    }

    public RecipeManager getRecipeManager() {
        return realLevel.getRecipeManager();
    }

    public MapItemSavedData getMapData(String s) {
        return realLevel.getMapData(s);
    }

    public void setMapData(String s, MapItemSavedData d) {}

    public int getFreeMapId() {
        return realLevel.getFreeMapId();
    }

    public void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4) {}

    public void playSeededSound(@Nullable Player var1, Entity var2, Holder<SoundEvent> var3, SoundSource var4, float var5, float var6, long var7) {}

    public String gatherChunkSourceStats() {
        return realLevel.gatherChunkSourceStats();
    }

    @Nullable
    public Entity getEntity(int var1) {
        return realLevel.getEntity(var1);
    }

    @Nullable
    public Entity getEntity(UUID id) {
        return realLevel.getEntity(id);
    }

    public LevelEntityGetter<Entity> getEntities() {
        return realLevel.getEntities();
    }

    public List<Entity> getEntities(@Nullable Entity e, AABB ab, Predicate<? super Entity> p) {
        return realLevel.getEntities(e, ab, p);
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> test, AABB ab, Predicate<? super T> p, List<? super T> l, int i) {
        realLevel.getEntities(test, ab, p, l, i);
    }

    public void destroyBlockProgress(int var1, BlockPos var2, int var3) {}

    public ServerScoreboard getScoreboard() {
        return realLevel.getScoreboard();
    }

    //levelAccessor

    public void gameEvent(GameEvent var1, Vec3 var2, GameEvent.Context var3) {}

    public void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12) {
        realLevel.addParticle(var1, var2, var4, var6, var8, var10, var12);
    }

    public ServerChunkCache getChunkSource() {
        if (this.getLevelData() instanceof UseServerLevelData data) return data.real.getChunkSource();
        return realLevel.getChunkSource();
    }

    //levelReader

    public FeatureFlagSet enabledFeatures() {
        return realLevel.enabledFeatures();
    }

    public int getSeaLevel() {
        return realLevel.getSeaLevel();
    }

    public Holder<Biome> getUncachedNoiseBiome(int var1, int var2, int var3) {
        return realLevel.getUncachedNoiseBiome(var1, var2, var3);
    }

    //blockAndTintGetter

    public float getShade(Direction var1, boolean var2) {
        return realLevel.getShade(var1, var2);
    }

    public LevelLightEngine getLightEngine() {
        return realLevel.getLightEngine();
    }

    public int getBlockTint(BlockPos var1, ColorResolver var2) {
        return realLevel.getBlockTint(var1, var2);
    }

    //scheduledTickAcceess

    public LevelTicks<Block> getBlockTicks() {
        return realLevel.getBlockTicks();
    }

    public LevelTicks<Fluid> getFluidTicks() {
        return realLevel.getFluidTicks();
    }
}
