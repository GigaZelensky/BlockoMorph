package net.blockomorph.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.function.BiConsumer;

/*
 *             //TEMP CLASS until the GUI refactoring is done
 */
public class GuiBlockRenderer extends PictureInPictureRenderer<GuiBlockRenderState> {
	public GuiBlockRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	public Class<GuiBlockRenderState> getRenderStateClass() {
		return GuiBlockRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiBlockRenderState pictureInPictureRenderState, PoseStack poseStack) {
		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
		pictureInPictureRenderState.action().accept(this.bufferSource, poseStack);
	}

	@Override
	protected String getTextureLabel() {
		return "block";
	}

	protected float getTranslateY(int i, int j) {
		return MorphScreen.getTranslateY(i, j);
	}
}
