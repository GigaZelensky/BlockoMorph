package net.blockomorph.mixins.debug;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(value = {
		ChunkRenderDispatcher.class,
		ModelBlockRenderer.class,
		BlockRenderDispatcher.class,
		BlockRenderDispatcher.class,
		ItemBlockRenderTypes.class
})
public class Dump {
}
