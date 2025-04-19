package net.blockomorph.mixins;

import net.blockomorph.utils.BlockBracker;
import net.blockomorph.utils.BlockInPlayer;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", shift = At.Shift.BEFORE, ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void redirectClick(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand interactionhand) {
        if (MorphUtils.hit != null && MorphUtils.hit.getEntity() instanceof PlayerAccessor pl && pl.isFullActive()) {
            for (BlockInPlayer br : pl.getBlocksData().values()) {
                if (br.getBlockBracker().players.contains(this.player)) {
                    ci.cancel();
                    return;
                }
            }
            if (MorphUtils.performClientUse(pl, interactionhand)) {
                ci.cancel();
            }
        }
    }
}
