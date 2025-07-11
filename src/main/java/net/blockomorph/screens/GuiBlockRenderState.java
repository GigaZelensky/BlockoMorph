package net.blockomorph.screens;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

/*
 *             //TEMP CLASS until the GUI refactoring is done
 */
public class GuiBlockRenderState implements PictureInPictureRenderState {
	private final int SCRISSORS_SIZE = 70;
	private final ScreenRectangle scrissorsArea;
	private final ScreenRectangle bounds;
	private final int x0;
	private final int y0;
	private final int x1;
	private final int y1;
	private final float scale;
	private final BiConsumer<MultiBufferSource.BufferSource, PoseStack> renderDo;
	private final int x;
	private final int y;

	public GuiBlockRenderState(GuiGraphics gui, int x, int y, float scale, BiConsumer<MultiBufferSource.BufferSource, PoseStack> renderOutput) {
		this.x = x;
		this.y = y;
		this.x0 = x - SCRISSORS_SIZE;
		this.x1 = x + SCRISSORS_SIZE;
		this.y0 = y - SCRISSORS_SIZE;
		this.y1 = y + SCRISSORS_SIZE;
		this.scale = scale;
		this.scrissorsArea = gui.peekScissorStack();
		this.bounds = PictureInPictureRenderState.getBounds(this.x0, this.y0, this.x1, this.y1, this.scrissorsArea);
		this.renderDo = renderOutput;
	}

	@Override
	public int x0() {
		return this.x0;
	}

	@Override
	public int x1() {
		return this.x1;
	}

	@Override
	public int y0() {
		return this.y0;
	}

	@Override
	public int y1() {
		return this.y1;
	}

	@Override
	public float scale() {
		return this.scale;
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return this.scrissorsArea;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		return this.bounds;
	}

	public void startRender(PoseStack stack, MultiBufferSource.BufferSource buffer) {
		//Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
		this.renderDo.accept(buffer, stack);
		buffer.endLastBatch();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof GuiBlockRenderState state) {
			return Objects.equals(this.x, state.x) && Objects.equals(this.y, state.y);
		}
		return false;
	}
}