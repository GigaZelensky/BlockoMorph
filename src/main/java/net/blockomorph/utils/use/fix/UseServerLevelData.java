package net.blockomorph.utils.use.fix;

import net.blockomorph.utils.use.UseServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UseServerLevelData implements ServerLevelData {
    public final ServerLevel real;
    private final ServerLevelData data;

    public UseServerLevelData(ServerLevel orig) {
        this.real = orig;
        this.data = (ServerLevelData) orig.getLevelData();
    }
    @Override
    public String getLevelName() {
        return data.getLevelName();
    }

    @Override
    public void setThundering(boolean yes) {
        data.setThundering(yes);
    }

    @Override
    public int getRainTime() {
        return data.getRainTime();
    }

    @Override
    public void setRainTime(int i) {
        data.setRainTime(i);
    }

    @Override
    public void setThunderTime(int i) {
        data.setThunderTime(i);
    }

    @Override
    public int getThunderTime() {
        return data.getThunderTime();
    }

    @Override
    public int getClearWeatherTime() {
        return data.getClearWeatherTime();
    }

    @Override
    public void setClearWeatherTime(int i) {
        data.setClearWeatherTime(i);
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return data.getWanderingTraderSpawnDelay();
    }

    @Override
    public void setWanderingTraderSpawnDelay(int i) {
        data.setWanderingTraderSpawnDelay(i);
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return data.getWanderingTraderSpawnChance();
    }

    @Override
    public void setWanderingTraderSpawnChance(int i) {
        data.setWanderingTraderSpawnChance(i);
    }

    @Override
    public @Nullable UUID getWanderingTraderId() {
        return data.getWanderingTraderId();
    }

    @Override
    public void setWanderingTraderId(@NotNull UUID uuid) {
        data.setWanderingTraderId(uuid);
    }

    @Override
    public @NotNull GameType getGameType() {
        return data.getGameType();
    }

    @Override
    public void setWorldBorder(WorldBorder.@NotNull Settings settings) {
        data.setWorldBorder(settings);
    }

    @Override
    public WorldBorder.@NotNull Settings getWorldBorder() {
        return data.getWorldBorder();
    }

    @Override
    public boolean isInitialized() {
        return data.isInitialized();
    }

    @Override
    public void setInitialized(boolean yes) {
        data.setInitialized(yes);
    }

    @Override
    public boolean getAllowCommands() {
        return data.getAllowCommands();
    }

    @Override
    public void setGameType(@NotNull GameType gamemode) {
        data.setGameType(gamemode);
    }

    @Override
    public @NotNull TimerQueue<MinecraftServer> getScheduledEvents() {
        return data.getScheduledEvents();
    }

    @Override
    public void setGameTime(long l) {
        data.setGameTime(l);
    }

    @Override
    public void setDayTime(long l) {
        data.setDayTime(l);
    }

    @Override
    public void setXSpawn(int x) {
        data.setXSpawn(x);
    }

    @Override
    public void setYSpawn(int y) {
        data.setYSpawn(y);
    }

    @Override
    public void setZSpawn(int z) {
        data.setZSpawn(z);
    }

    @Override
    public void setSpawnAngle(float angle) {
        data.setSpawnAngle(angle);
    }

    @Override
    public int getXSpawn() {
        return data.getXSpawn();
    }

    @Override
    public int getYSpawn() {
        return data.getYSpawn();
    }

    @Override
    public int getZSpawn() {
        return data.getZSpawn();
    }

    @Override
    public float getSpawnAngle() {
        return data.getSpawnAngle();
    }

    @Override
    public long getGameTime() {
        return data.getGameTime();
    }

    @Override
    public long getDayTime() {
        return data.getDayTime();
    }

    @Override
    public boolean isThundering() {
        return data.isThundering();
    }

    @Override
    public boolean isRaining() {
        return data.isRaining();
    }

    @Override
    public void setRaining(boolean yes) {
        data.setRaining(yes);
    }

    @Override
    public boolean isHardcore() {
        return data.isHardcore();
    }

    @Override
    public @NotNull GameRules getGameRules() {
        return data.getGameRules();
    }

    @Override
    public @NotNull Difficulty getDifficulty() {
        return data.getDifficulty();
    }

    @Override
    public boolean isDifficultyLocked() {
        return data.isDifficultyLocked();
    }
}
