package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.accessors.BlockEntityAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelDataManager.class)
public class ModelDataManagerMixin {

    @Inject(method = "requestRefresh", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void redirect(BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof BlockEntityAccessor acc) {
            UseController ctr = acc.getController();
            if (ctr != null) {
                ci.cancel();
                ctr.updateModelData(blockEntity.getModelData());
            }
        }
    }
}
