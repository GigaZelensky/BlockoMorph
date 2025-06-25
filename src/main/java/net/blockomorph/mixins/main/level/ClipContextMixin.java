package net.blockomorph.mixins.main.level;

import net.blockomorph.utils.accessors.ClipContextAccessor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClipContext.class)
public class ClipContextMixin implements ClipContextAccessor {

	@Shadow @Final private CollisionContext collisionContext;
	@Shadow @Final private ClipContext.Block block;

	public CollisionContext getContext() {
		return this.collisionContext;
	}

	public ClipContext.Block getMode() {
		return this.block;
	}
}
