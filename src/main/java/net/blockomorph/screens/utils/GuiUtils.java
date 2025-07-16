package net.blockomorph.screens.utils;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.blockomorph.BlockomorphServer;
import net.blockomorph.utils.MorphUtils;
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
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiUtils { //Cross-platform wrapper
	public static final BlockPos AIR = new BlockPos(0, 500, 0);
	private static final Minecraft MC = Minecraft.getInstance();
	public static final MultiBufferSource bufferSource = MC.renderBuffers().bufferSource();
	private static final BlockRenderDispatcher blockRenderer = MC.getBlockRenderer();
	private static final BlockEntityRenderDispatcher blockEntityRenderer = MC.getBlockEntityRenderDispatcher();
	private ItemStackRenderState scratchItemStackRenderState;
	private GuiGraphics GUI;
	private int mouseX;
	private int mouseY;
	private float tick;
	private Font font;

	public static ResourceLocation res(String path) {
		return MorphUtils.res(path);
	}

	public static ResourceLocation vanillaRes(String path) {
		return MorphUtils.vanillaRes(path);
	}

	public void setGuiGraphics(GuiGraphics gui, Font font, int mouseX, int mouseY, float tick) {
		GUI = gui;
		this.font = font;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tick = tick;
		this.scratchItemStackRenderState = new ItemStackRenderState();
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
		this.renderTooltip(List.of(text), mouseX, mouseY);
	}

	public void renderTooltip(List<Component> texts, int mouseX, int mouseY) {
		GUI.renderComponentTooltip(this.font, texts, mouseX, mouseY);
	}

	public void renderSprite(ResourceLocation resourceLocation, int x, int y, int maxSizeX, int maxSizeY) {
		GUI.blitSprite(RenderType::guiTextured, resourceLocation, x, y, maxSizeX, maxSizeY);
	}

	public void drawString(Component text, int x, int y, int color, boolean useShadow) {
		GUI.drawString(this.font, text, x, y, color, useShadow);
	}

	public void fill(int x, int y, int endX, int endY, int color) {
		GUI.fill(x, y, endX, endY, color);
	}

	//HINT:   XY - upper left corner of item
	public void renderItem(ItemStack item, float x, float y, float scale, float zDepth) {
		if (scale == 1) scale = 16f;
		PoseStack pose = GUI.pose();
		pose.pushPose();

		MC.getItemModelResolver().updateForTopItem(this.scratchItemStackRenderState, item, ItemDisplayContext.GUI, MC.level, MC.player, 0);
		pose.translate(x + 8, y + 8, 150 + zDepth);
		pose.scale(scale, -scale, scale);

		this.doMainRenderingItem(pose);

		pose.popPose();
	}

	private void doMainRenderingItem(PoseStack stack) {
		boolean bl = !this.scratchItemStackRenderState.usesBlockLight();
		if (bl) {
			GUI.flush();
			Lighting.setupForFlatItems();
		}

		this.scratchItemStackRenderState.render(stack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
		GUI.flush();
		if (bl) {
			Lighting.setupFor3DItems();
		}
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
		}
	}

	public void renderAdditionalOnBlock(BlockState blockState, float x, float y, float scale) {
		if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
			Item item = null;
			if (blockState.getBlock() instanceof LiquidBlock) {
				item = blockState.getFluidState().getType().getBucket();
			} else if (blockState.getBlock().asItem() != Items.AIR) {
				item = blockState.getBlock().asItem();
			}
			if (item != null) {
				ItemStack itemStack = new ItemStack(item);
				Map<String, String> map = new HashMap<>();
				for (Property<?> property : blockState.getProperties()) {
					map.put(property.getName(), blockState.getValue(property).toString());
				}
				itemStack.set(DataComponents.BLOCK_STATE, new BlockItemStateProperties(map));
				this.renderItem(itemStack, x, y, scale, 100);
			}
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

	public static boolean isMouseOver(int x, int y, int endX, int endY, double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= endX && mouseY >= y && mouseY < endY;
	}
}
