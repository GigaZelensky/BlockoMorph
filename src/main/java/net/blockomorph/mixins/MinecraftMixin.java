package net.blockomorph.mixins;

import net.blockomorph.utils.BlockInPlayer;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.use.UseController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public LocalPlayer player;

    @Shadow protected abstract void addCustomNbtData(ItemStack p_263370_, BlockEntity p_263368_);

    @Shadow @Nullable public MultiPlayerGameMode gameMode;

    @Inject(method = "startUseItem()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", shift = At.Shift.BEFORE, ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void redirectClick(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand interactionhand) {
        if (MorphUtils.playerHitResult != null) {
            for (BlockInPlayer br : MorphUtils.hitEntity.getBlocksData().values()) {
                if (br.getBlockBracker().players.contains(this.player)) {
                    ci.cancel();
                    return;
                }
            }//TODO
            if (MorphUtils.performClientUse(MorphUtils.hitEntity, interactionhand)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "pickBlock", at = @At(value = "HEAD"))
    public void redirectPick(CallbackInfo ci) {
        if (MorphUtils.playerHitResult != null) {
            boolean flag = this.player.getAbilities().instabuild;
            UseController ctr = MorphUtils.hitEntity.getUseControllers().get(MorphUtils.hitPart);

            ItemStack itemstack = ctr.getBlockState().getCloneItemStack(MorphUtils.playerHitResult.boundBlockPos(), ctr.getUseLevel(), ctr.getOffset(), this.player);
            if (!itemstack.isEmpty()) {
                if (flag && Screen.hasControlDown() && ctr.getBlockEntity() != null) {
                    this.addCustomNbtData(itemstack, ctr.getBlockEntity());
                }
                Inventory inventory = this.player.getInventory();
                int i = inventory.findSlotMatchingItem(itemstack);
                if (flag) {
                    inventory.setPickedItem(itemstack);
                    this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
                } else if (i != -1) {
                    if (Inventory.isHotbarSlot(i)) {
                        inventory.selected = i;
                    } else {
                        this.gameMode.handlePickItem(i);
                    }
                }
            }
        }
    }
}
