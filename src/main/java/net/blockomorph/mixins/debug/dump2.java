package net.blockomorph.mixins.debug;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(value = {ContainerOpenersCounter.class, Level.class})
public class dump2 {
}
