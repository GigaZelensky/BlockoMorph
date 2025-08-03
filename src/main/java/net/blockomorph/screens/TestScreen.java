package net.blockomorph.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestScreen extends Screen {
	public TestScreen() {
		super(CommonComponents.GUI_DONE);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.guiRenderState.submitPicturesInPictureState(new GuiBlockRenderState(guiGraphics, this.width/2, this.height/2, 25, (buffer, posestack) -> {
			this.renderBlock(posestack, buffer, Blocks.LECTERN.defaultBlockState());
		}));
	}

	private void renderBlock(PoseStack poseStack, MultiBufferSource bufferSource, BlockState blockState) {
		//poseStack.translate(this.leftPos + 42 + xO*36, this.topPos + 41.8 + yO*36, 100);
		//poseStack.mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
		//poseStack.scale(20.0F, 20.0F, 20.0F);
		//poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
		//poseStack.mulPose(Axis.YP.rotationDegrees(-45.0F));
		//poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

		BlockPos pos = new BlockPos(0, 500, 0);
		RandomSource random = RandomSource.create(blockState.getSeed(pos));
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			List<BlockModelPart> list = this.dispatcher.getBlockModel(blockState).collectParts(random);
			var renderType = ItemBlockRenderTypes.getMovingBlockRenderType(blockState);
			this.dispatcher.getModelRenderer().tesselateBlock(Minecraft.getInstance().level, list, blockState, pos, poseStack, bufferSource.getBuffer(renderType), false, OverlayTexture.NO_OVERLAY);
		}
		//this.renderBlockEntity(blockState, ticks, poseStack, bufferSource, tag);
	}

	private final BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
}
