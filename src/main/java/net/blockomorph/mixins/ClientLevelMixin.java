package net.blockomorph.mixins;

import net.blockomorph.utils.BlockInPlayer;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Shadow protected abstract LevelEntityGetter<Entity> getEntities();

    @Inject(method = "doAnimateTick", at = @At(value = "TAIL"))
    public void animatePlayers(int p_233613_, int p_233614_, int p_233615_, int p_233616_, RandomSource p_233617_, Block p_233618_, BlockPos.MutableBlockPos pos, CallbackInfo ci) {
        AABB blockAABB = new AABB(pos);
        this.getEntities().get(blockAABB, (entity) -> {
            if (entity instanceof PlayerAccessor pl && pl.isFullActive()) {
                for (BlockInPlayer bl : pl.getBlocksData().values()) {
                    if (blockAABB.contains(bl.getUseController().getRealPos())) {
                        bl.animateTick();
                    }
                }
            }
        });
    }
}
