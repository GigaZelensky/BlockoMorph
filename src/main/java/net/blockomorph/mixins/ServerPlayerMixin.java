package net.blockomorph.mixins;

import com.mojang.authlib.GameProfile;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.blockomorph.utils.use.UseServerLevel;
import net.blockomorph.utils.use.fix.BedController;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    @Unique
    private final HashMap<ResourceKey<Level>, UseServerLevel> levels = new HashMap<>();

    public ServerPlayerMixin(Level p_250508_, BlockPos p_250289_, float p_251702_, GameProfile p_252153_) {
        super(p_250508_, p_250289_, p_251702_, p_252153_);
    }

    @Inject(method = "stopSleepInBed", at = @At(value = "HEAD"), cancellable = true)
    public void stopSleepInBed(boolean p_9165_, boolean p_9166_, CallbackInfo ci) {
        BedController bd = ((PlayerAccessor)this).getBedController();
        if (bd != null) {
            if (bd.isWorking()) {
                ci.cancel();
                bd.stopSleep(true);
            }
        }
    }

    @ModifyVariable(
            method = "setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BlockPos modifyBlockPos(BlockPos originalPos) {
        if (originalPos instanceof BlockPosAccessor acc) {
            UseController ctr = acc.getController();
            if (ctr != null) {
                this.forcePos = true;
                return BlockPos.containing(ctr.getRealPos());
            }
        }
        return originalPos;
    }

    @Unique
    private boolean forcePos;

    @ModifyVariable(
            method = "setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private boolean modifyForced(boolean forced) {
        if (forcePos) {
            forcePos = false;
            return true;
        }
        return forced;
    }
}
