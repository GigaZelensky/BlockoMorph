package net.blockomorph.mixins;

import com.mojang.datafixers.DataFixer;
import net.blockomorph.utils.accessors.ServerLevelAccessor;
import net.blockomorph.utils.use.UseServerLevel;
import net.blockomorph.utils.use.fix.UseServerLevelData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelAccessor {
    @Shadow @Final private ServerLevelData serverLevelData;
    @Unique
    private final List<Object> initArgs = new ArrayList<>();

    @Inject(method = "<init>", at = @At(value = "TAIL"), cancellable = true)
    public void init(
            MinecraftServer sv,
            Executor runnable,
            LevelStorageSource.LevelStorageAccess storageAccess,
            ServerLevelData p_215002_,
            ResourceKey<Level> p_215003_,
            LevelStem stem,
            ChunkProgressListener p_215005_,
            boolean p_215006_,
            long p_215007_,
            List<CustomSpawner> p_215008_,
            boolean p_215009_,
            RandomSequences random,
            CallbackInfo ci
    ) {
        initArgs.add(stem);
        initArgs.add(storageAccess);
    }

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
        if (this.serverLevelData instanceof UseServerLevelData data) {
            return data.real.getChunkSource();
        }
        return new ServerChunkCache(serverLevel, levelStorageAccess, dataFixer, structureTemplateManager, executor, chunkGenerator, viewDistance, simulationDistance, bl, chunkProgressListener, chunkStatusUpdateListener, dataStorageGetter);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/level/ServerLevel;Ljava/nio/file/Path;Lcom/mojang/datafixers/DataFixer;ZLjava/util/concurrent/Executor;)Lnet/minecraft/world/level/chunk/storage/EntityStorage;"))
    public EntityStorage replaceStorage(ServerLevel a1, Path a2, DataFixer a3, boolean a4, Executor a5) {
        if (this.serverLevelData instanceof UseServerLevelData data && data.real instanceof ServerLevelAccessor acc) {
            return (EntityStorage) acc.getInitArgs().get(0);
        }
        return new EntityStorage(a1, a2, a3, a4, a5);
    }

    @ModifyVariable(method = "<init>", at = @At(value = "STORE"))
    private EntityPersistentStorage<Entity> catchEntStorage(EntityPersistentStorage<Entity> value) {
        initArgs.add(value);
        return value;
    }
}
