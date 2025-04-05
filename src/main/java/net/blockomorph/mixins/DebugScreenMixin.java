package net.blockomorph.mixins;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;
import java.util.Locale;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenMixin {

    @Shadow protected HitResult block;

    @Redirect(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;", ordinal = 0))
    private HitResult.Type redirectGetType(HitResult instance) {
        if (instance.getType() == HitResult.Type.MISS) {
            if (MorphUtils.hit != null) {
                return HitResult.Type.BLOCK;
            }
        }
        return instance.getType();
    }

    @Redirect(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1))
    public boolean redirectAdd(List<String> instance, Object e) {
        if (MorphUtils.hitEntity instanceof PlayerAccessor pl) {
            Vec3 posittion = MorphUtils.getRealBlockPos(pl, MorphUtils.hitPart);
            String x = this.formatCoordinate(posittion.x);
            String y = this.formatCoordinate(posittion.y);
            String z = this.formatCoordinate(posittion.z);
            String st = String.format(Locale.ROOT, "Targeted Block: %s, %s, %s", x, y, z);
            return instance.add(ChatFormatting.UNDERLINE + st);
        }
        return instance.add((String) e);
    }

    private String formatCoordinate(double coordinate) {
        if (coordinate == (int) coordinate) {
            return String.valueOf((int) coordinate);
        } else {
            return String.format(Locale.ROOT, "%.3f", coordinate);
        }
    }

    @ModifyVariable(method = "getSystemInformation", at = @At(value = "STORE", ordinal = 0))
    private BlockState modifyBlockState(BlockState value) {
        if (MorphUtils.hitEntity instanceof PlayerAccessor pl) {
            return pl.getBlocks().get(MorphUtils.hitPart);
        }
        return value;
    }
}
