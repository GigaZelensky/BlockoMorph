package net.blockomorph.mixins.main;

import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	@Inject(method = "canBeSeenByAnyone", at = @At("RETURN"), cancellable = true)
	public void checkSeen(CallbackInfoReturnable<Boolean> cir) {
		boolean result = cir.getReturnValue();
		if (this instanceof PlayerAccessor pl && pl.isActive()) {
			cir.setReturnValue(false);
			return;
		}
		cir.setReturnValue(result);
	}

	@Inject(method = "push", at = @At("HEAD"), cancellable = true)
	public void rejectPush(Entity entity, CallbackInfo ci) {
		if (this instanceof PlayerAccessor pl && pl.isActive())
			ci.cancel();
	}

	@Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
	public void rejectPushEntities(CallbackInfo ci) {
		if (this instanceof PlayerAccessor pl && pl.isActive()) {
			ci.cancel();
		}
	}
}
