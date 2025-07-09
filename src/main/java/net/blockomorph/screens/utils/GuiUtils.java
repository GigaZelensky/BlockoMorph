package net.blockomorph.screens.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blockomorph.BlockomorphServer;
import net.blockomorph.utils.accessors.ClientLevelAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class GuiUtils { //Cross-platform wrapper
	protected static final BlockPos AIR = new BlockPos(0, 500, 0);
	private static final Minecraft MC = Minecraft.getInstance();
	public static final MultiBufferSource bufferSource = MC.renderBuffers().bufferSource();
	private static final BlockRenderDispatcher blockRenderer = MC.getBlockRenderer();
	private static final BlockEntityRenderDispatcher blockEntityRenderer = MC.getBlockEntityRenderDispatcher();
	private GuiGraphics GUI;
	private int mouseX;
	private int mouseY;
	private float tick;
	private Font font;

	public static ResourceLocation res(String path) {
		return ResourceLocation.fromNamespaceAndPath(BlockomorphServer.MOD_ID, path);
	}

	public static ResourceLocation vanillaRes(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}

	public static Path getSavedBlockManagerPath() {
		return FabricLoader.getInstance().getGameDir();
	}

	public void setGuiGraphics(GuiGraphics gui, Font font, int mouseX, int mouseY, float tick) {
		GUI = gui;
		this.font = font;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tick = tick;
	}

	public GuiGraphics getGuiGraphics() {
		return GUI;
	}

	public Font getFont() {
		return font;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public float getTick() {
		return tick;
	}

	/* HINT:
		X - up left corner
		Y - up left corner
		u - start of texture X (left up corner)
		y - start of texture Y (left up corner)
		uvMaxX - length of start of UV
		uvMaxY - length of start of UV
		max X - length
		max Y - height
	*/
	public void blit(ResourceLocation resourceLocation, int x, int y, float u, float v, int uvMaxX, int uvMaxY, int maxX, int maxY) {
		GUI.blit(RenderType::guiTextured, resourceLocation, x, y, u, v, uvMaxX, uvMaxY, maxX, maxY);
	}

	public void blitMonoImage(ResourceLocation resourceLocation, int x, int y, int maxSizeX, int maxSizeY) {
		this.blit(resourceLocation, x, y, 0, 0, maxSizeX, maxSizeY, maxSizeX, maxSizeY);
	}

	public void renderTooltip(Component text, int mouseX, int mouseY) {
		GUI.renderTooltip(this.font, text, mouseX, mouseY);
	}

	//HINT:   XY - down corner of block
	public void renderBlockInGui(BlockState blockState, @Nullable BlockEntity blockEntity, float x, float y, float scale) {
		PoseStack stack = GUI.pose();
		stack.pushPose();
		stack.translate(x, y, 100F);
		stack.scale(scale, -scale, scale);
		stack.mulPose(Axis.XP.rotationDegrees(30.0F));
		stack.mulPose(Axis.YP.rotationDegrees(-225.0F));
		this.renderBlock(stack, blockState);
		this.renderBlockEntity(stack, blockEntity);
		stack.popPose();
	}

	private void renderBlock(PoseStack stack, BlockState blockState) {
		RandomSource random = RandomSource.create(blockState.getSeed(AIR));
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			List<BlockModelPart> list = blockRenderer.getBlockModel(blockState).collectParts(random);
			var renderType = ItemBlockRenderTypes.getMovingBlockRenderType(blockState);
			ClientLevelAccessor acc = ClientLevelAccessor.of(MC.level);
			acc.setSpecialRenderingMode(true);
			blockRenderer.getModelRenderer().tesselateBlock(MC.level, list, blockState, AIR, stack, bufferSource.getBuffer(renderType), false, OverlayTexture.NO_OVERLAY);
			acc.setSpecialRenderingMode(false);
		} else if (blockState.getBlock().asItem() == Items.AIR) {
			//TODO
		}
	}

	private <T extends BlockEntity> void renderBlockEntity(PoseStack stack, T blockEntity) {
		if (blockEntity != null) {
			BlockEntityRenderer<T> renderer = blockEntityRenderer.getRenderer(blockEntity);
			if (renderer != null) {
				try {
					Camera cam = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera; //TODO
					ClientLevelAccessor acc = ClientLevelAccessor.of(MC.level);
					acc.setSpecialRenderingMode(true);
					renderer.render(blockEntity, this.tick, stack, bufferSource, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY, cam.getPosition());
					acc.setSpecialRenderingMode(false);
				} catch (Exception e) {
					//TODO
				}
			}
		}
	}
}
