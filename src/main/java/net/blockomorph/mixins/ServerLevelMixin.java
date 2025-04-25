package net.blockomorph.mixins;

import com.mojang.datafixers.DataFixer;
import net.blockomorph.utils.accessors.ServerLevelAccessor;
import net.blockomorph.utils.use.ProxyServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelAccessor {
    @Shadow public abstract Explosion explode(@Nullable Entity p_256039_, @Nullable DamageSource p_255778_, @Nullable ExplosionDamageCalculator p_256002_, double p_256067_, double p_256370_, double p_256153_, float p_256045_, boolean p_255686_, Level.ExplosionInteraction p_255827_);

    private final List<Object> initArgs = new ArrayList<>();

    @Inject(method = "<init>", at = @At(value = "TAIL"), cancellable = true)
    public void init(
            MinecraftServer sv,
            Executor runnable,
            LevelStorageSource.LevelStorageAccess p_215001_,
            ServerLevelData p_215002_,
            ResourceKey p_215003_,
            LevelStem stem,
            ChunkProgressListener p_215005_,
            boolean p_215006_,
            long p_215007_,
            List p_215008_,
            boolean p_215009_,
            RandomSequences random,
            CallbackInfo ci
    ) {
        initArgs.add(stem);
    }

    /*@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;forceSynchronousWrites()Z"), cancellable = true)
    public void init2(
            MinecraftServer sv,
            Executor runnable,
            LevelStorageSource.LevelStorageAccess p_215001_,
            ServerLevelData p_215002_,
            ResourceKey p_215003_,
            LevelStem stem,
            ChunkProgressListener p_215005_,
            boolean p_215006_,
            long p_215007_,
            List p_215008_,
            boolean p_215009_,
            RandomSequences random,
            CallbackInfo ci
    ) {
        initArgs.add(stem);
        ci.cancel();
    }*/

    @Override
    public List<Object> getInitArgs() {
        return this.initArgs;
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/level/ServerChunkCache"))
    private ServerChunkCache replaceServerChunkCache(
            ServerLevel serverLevel,
            LevelStorageSource.LevelStorageAccess levelStorageAccess,
            DataFixer dataFixer,
            StructureTemplateManager structureTemplateManager,
            Executor executor,
            ChunkGenerator chunkGenerator,
            int viewDistance,
            int simulationDistance,
            boolean bl,
            ChunkProgressListener chunkProgressListener,
            ChunkStatusUpdateListener chunkStatusUpdateListener,
            Supplier<DimensionDataStorage> dataStorageGetter
    ) {
        if (serverLevel instanceof ProxyServerLevel lv) {
            return lv.getRealLevel().getChunkSource();
        }
        return new ServerChunkCache(serverLevel, levelStorageAccess, dataFixer, structureTemplateManager, executor, chunkGenerator, viewDistance, simulationDistance, bl, chunkProgressListener, chunkStatusUpdateListener, dataStorageGetter);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/level/ServerLevel;Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;ZLjava/util/concurrent/Executor;)Lnet/minecraft/world/level/chunk/storage/EntityStorage;"))
    public EntityStorage replaceStorage(ServerLevel a1, Path a2, DataFixer a3, boolean a4, Executor a5) {
        if (a1 instanceof ProxyServerLevel) {
            return new EntityStorage(a1, a2, a3, a4, a5) {
                @Override
                public void storeEntities(ChunkEntities<Entity> p_156559_) {}

                @Override
                public void flush(boolean p_182487_) {}
            };
        }
        return new EntityStorage(a1, a2, a3, a4, a5);
    }
}
