package net.blockomorph.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/*
 *             //TEMP CLASS until the GUI refactoring is done
 */
public class GuiBlockRenderState implements PictureInPictureRenderState {

	public GuiBlockRenderState(int x, int y, int x0, int y0, @Nullable ScreenRectangle scissorArea, BiConsumer<MultiBufferSource.BufferSource, PoseStack> act) {
		this(
				x, // x0
				y, // x1
				x0, // y0
				y0, // y1
				20f, // scale
				scissorArea,
				PictureInPictureRenderState.getBounds(x, y, x0, y0, scissorArea),
				act
		);
	}

	@Override
	public int x0() {
		return 0;
	}

	@Override
	public int x1() {
		return 0;
	}

	@Override
	public int y0() {
		return 0;
	}

	@Override
	public int y1() {
		return 0;
	}

	@Override
	public float scale() {
		return 0;
	}

	@Override
	public @Nullable ScreenRectangle scissorArea() {
		return null;
	}

	@Override
	public @Nullable ScreenRectangle bounds() {
		return null;
	}
}
