package net.blockomorph.mixins.compat.vs2;

import net.blockomorph.utils.MorphUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Debug(export = true)
@Mixin(targets = "org.valkyrienskies.mod.common.VSGameUtilsKt", remap = false)
public abstract class VS2_BM_VsUtilsMixin {

	@Inject(method = "squaredDistanceBetweenInclShips", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
	private static void normalizeCoords(Level lv, double x, double y, double z, double x2, double y2, double z2, CallbackInfoReturnable<Double> cir,
							double dx, double dy, double dz, double x3, double y3, double z3, double x4, double y4, double z4
	) {
		MorphUtils.distanceTo(new Vec3(x3, y3, z3), new Vec3(x4, y4, z4), true, 0, cir::setReturnValue);
	}

}
