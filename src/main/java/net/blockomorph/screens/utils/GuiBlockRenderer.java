package net.blockomorph.screens.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.accessors.ClientLevelAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

import static net.blockomorph.screens.utils.GuiUtils.MC;

public class GuiBlockRenderer extends PictureInPictureRenderer<GuiBlockRenderState> {
	public GuiBlockRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	public Class<GuiBlockRenderState> getRenderStateClass() {
		return GuiBlockRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiBlockRenderState guiState, PoseStack stack) {
		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
		stack.mulPose(Axis.XP.rotationDegrees(30.0F));
		stack.mulPose(Axis.YP.rotationDegrees(-45.0F));
		stack.mulPose(Axis.ZP.rotationDegrees(180.0F));


		BlockEntity blockEntity = guiState.getBlockEntity();
		this.renderBlock(stack, guiState.getState(), blockEntity != null ? blockEntity.getBlockPos() : GuiUtils.AIR);
		this.renderBlockEntity(guiState.getDeltaTick(), stack, blockEntity);
	}

	private void renderBlock(PoseStack stack, BlockState blockState, BlockPos pos) {
		RandomSource random = RandomSource.create(blockState.getSeed(pos));
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			List<BlockModelPart> list = GuiUtils.blockRenderer.getBlockModel(blockState).collectParts(random);
			var renderType = ItemBlockRenderTypes.getMovingBlockRenderType(blockState);
			ClientLevelAccessor acc = ClientLevelAccessor.of(MC.level);
			acc.setSpecialRenderingMode(true);
			GuiUtils.blockRenderer.getModelRenderer().tesselateBlock(MC.level, list, blockState, pos, stack, bufferSource.getBuffer(renderType), false, OverlayTexture.NO_OVERLAY);
			acc.setSpecialRenderingMode(false);
		}
	}

	private <T extends BlockEntity> void renderBlockEntity(float delta, PoseStack stack, T blockEntity) {
		if (blockEntity != null) {
			BlockEntityRenderer<T> renderer = GuiUtils.blockEntityRenderer.getRenderer(blockEntity);
			if (renderer != null) {
				ClientLevelAccessor acc = ClientLevelAccessor.of(MC.level);
				try {
					Camera cam = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
					acc.setSpecialRenderingMode(true);
					renderer.render(blockEntity, delta, stack, bufferSource, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY, cam.getPosition());//TODO
				} catch (Exception ignored) {
				} finally {
					acc.setSpecialRenderingMode(false);
				}
			}
		}
	}

	@Override
	protected float getTranslateY(int length, int height) {
		return (float) length /2;
	}

	@Override
	protected String getTextureLabel() {
		return MorphUtils.res("block and blockentity").toString();
	}
}
