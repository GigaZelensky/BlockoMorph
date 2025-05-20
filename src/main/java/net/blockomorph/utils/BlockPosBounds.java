package net.blockomorph.utils;

import net.blockomorph.Blockomorph;
import net.blockomorph.network.ClientBoundBlockPosBoundPacket;
import net.blockomorph.network.ClientBoundConfigUpdatePacket;
import net.blockomorph.utils.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber
public class BlockPosBounds { //TODO
    private static File FILE;
    public static final int MAX_PLAYER_CHUNK_COUNT = 31;
    public static final int MAX_PLAYER_CHUNKS = 193548;
    private static final RandomSource random = RandomSource.create();

    private static final HashMap<UUID, ChunkPos> BOUNDS = new HashMap<>();
    private static final HashMap<UUID, ChunkPos> CLIENT_BOUNDS = new HashMap<>();

    public static void boundPlayer(Player player) {
        if (player instanceof ServerPlayer) {
            UUID uuid = player.getUUID();
            if (!BOUNDS.containsKey(uuid)) {
                ChunkPos pos = getChunkPos();
                BOUNDS.put(uuid, pos);
                save();
            }
        }
    }

    private static ChunkPos getChunkPos() {
        while (true) {
            int x = random.nextInt(MAX_PLAYER_CHUNKS);
            int z = random.nextInt(MAX_PLAYER_CHUNKS);
            ChunkPos pos = new ChunkPos(x, z);
            if (!BOUNDS.containsValue(pos)) {
                return pos;
            }
        }
    }

    @Nullable
    public static ChunkPos getChunkPosForPlayer(Player player) {
        if (player.level().isClientSide)
            return CLIENT_BOUNDS.get(player.getUUID());
        return BOUNDS.get(player.getUUID());
    }

    private static void initFile() {
        if (Config.getServer() != null) {
            FILE = new File(Config.getServer().getWorldPath(new LevelResource("data")).toFile(), "blockomorph.dat");
        }
    }

    static void load() {
        initFile();
        if (FILE == null || !FILE.exists()) return;
        CompoundTag tg;
        try {
            tg = NbtIo.readCompressed(FILE);
        } catch (IOException ex) {
            Blockomorph.LOGGER.fatal("Unable to read PLAYER-BLOCKPOS mappings.", ex);
            return;
        }
        for (String key : tg.getAllKeys()) {
            BOUNDS.put(UUID.fromString(key), new ChunkPos(tg.getLong(key)));
        }
    }

    private static void save() {
        if (FILE == null) return;
        CompoundTag tg = new CompoundTag();
        BOUNDS.forEach(((uuid, chunkPos) -> {
            tg.putLong(uuid.toString(), chunkPos.toLong());
        }));
        try {
            NbtIo.writeCompressed(tg, FILE);
        } catch (IOException ex) {
            Blockomorph.LOGGER.fatal("Unable to save PLAYER-BLOCKPOS mappings.", ex);
        }
    }

    @SubscribeEvent
    public static void onTrackStart(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer pl && event.getEntity() instanceof ServerPlayer looker) {
            UUID uuid = pl.getUUID();
            if (BOUNDS.containsKey(uuid)) {
                MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(uuid, BOUNDS.get(uuid)), looker);
            }
        }
    }

    @SubscribeEvent
    public static void onTrackStop(PlayerEvent.StopTracking event) {
        if (event.getTarget() instanceof ServerPlayer pl && event.getEntity() instanceof ServerPlayer looker) {
            MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(pl.getUUID(), null), looker);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        BOUNDS.clear();
        CLIENT_BOUNDS.clear();
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        boundPlayer(event.getEntity());
        ChunkPos pos = getChunkPosForPlayer(event.getEntity());
        if (pos != null) {
            MorphUtils.sendPlayer(new ClientBoundBlockPosBoundPacket(event.getEntity().getUUID(), pos), (ServerPlayer) event.getEntity());
        }
    }

    public static void handleBlockPosBound(ClientBoundBlockPosBoundPacket packet) {
        UUID uuid = packet.uuid;
        ChunkPos pos = packet.pos;
        if (pos.x == Integer.MAX_VALUE) {
            CLIENT_BOUNDS.remove(uuid);
        } else {
            CLIENT_BOUNDS.put(uuid, pos);
        }
    }
}
