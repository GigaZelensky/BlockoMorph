package net.blockomorph.utils.use;


import net.blockomorph.utils.accessors.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ProxyServerLevel extends ServerLevel {
    public final Path WRITER_DISABLER = Path.of("/..@    WRITE_DISABLED    ..@!\\❌");
    protected final ServerLevel realLevel;

    public ProxyServerLevel(ServerLevel real) {
        super(
                real.getServer(),
                (runnale) -> {},
                null,
                (ServerLevelData) real.getLevelData(),
                real.dimension(),
                (LevelStem) ((ServerLevelAccessor)real).getInitArgs().get(0),
                getChunkListener(),
                false,
                0L,
                List.of(),
                false,
                getRandomS()
        );
        this.realLevel = real;
    }

    public ServerLevel getRealLevel() {
        return this.realLevel;
    }


    //main methods

    protected void initCapabilities() {}

    public void tickCustomSpawners(boolean p_8800_, boolean p_8801_) {}

    //constructor

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
            public void onStatusChange(ChunkPos p_9618_, @Nullable ChunkStatus p_9619_) {}//chunk state update(loading, ready, etc)

            @Override
            public void start() {} //chunk loading start

            @Override
            public void stop() {}//chunk unload
        };
    }
}
