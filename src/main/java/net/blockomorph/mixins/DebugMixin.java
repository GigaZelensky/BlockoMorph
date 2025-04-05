package net.blockomorph.mixins;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export = true)
@Mixin(Container.class)
public interface DebugMixin {

    /*@Redirect(
            method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;I)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(DDD)D"
            )
    )
    private static double includeShipsInDistanceCheck(
            Player instance, double x, double y, double z) {
        return 0f;
    }

    @Inject(method = "stillValidBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/player/Player;I)Z", at = @At(value = "HEAD"))
    private static void test(BlockEntity p_272877_, Player p_272670_, int p_273411_, CallbackInfoReturnable<Boolean> cir) {
        throw new RuntimeException("Dictionary!");
    }*/

}