package net.blockomorph.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blockomorph.screens.MorphScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@org.spongepowered.asm.mixin.Debug(export = true)
@Mixin(GuiEntityRenderer.class)
public abstract class Debug extends PictureInPictureRenderer {

	protected Debug(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Inject(method = "renderToTexture(Lnet/minecraft/client/gui/render/state/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", at = @At(shift = At.Shift.AFTER, value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"), cancellable = true)
	public void run(GuiEntityRenderState guiEntityRenderState, PoseStack poseStack, CallbackInfo ci) {
		//if (true) return;
		ci.cancel();
		Vec3 vec = MorphScreen.test();
		poseStack.translate(vec.x, vec.y, vec.z);
		BlockState blockState = Blocks.STONE.defaultBlockState();
		RandomSource s = RandomSource.create();
		ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
		var renderType = ItemBlockRenderTypes.getMovingBlockRenderType(blockState);
		List<BlockModelPart> list = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState).collectParts(s);
		renderer.tesselateBlock(Minecraft.getInstance().level, list, blockState, new BlockPos(0, 500, 0), poseStack, this.bufferSource.getBuffer(renderType), false, OverlayTexture.NO_OVERLAY);
	}
}
