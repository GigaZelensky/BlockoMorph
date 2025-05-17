package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.hit.MorphedPlayerHitResult;
import net.blockomorph.utils.hit.PlayerHitResult;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;

    @Redirect(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;"))
    public BlockHitResult getHit(BlockGetter instance, ClipContext ctx) {
        if (this.entity instanceof Player pl) {
            MorphedPlayerHitResult hit = PlayerHitResult.calculateMorphedPlayerHitResult(pl, ctx.getFrom(), ctx.getTo(), Vec3.ZERO, ClipContext.Block.VISUAL, true);
            if (hit != null) {
                return hit;
            }
        }
        return instance.clip(ctx);
    }
}
