package net.blockomorph.screens.utils;

import net.blockomorph.screens.MorphScreen2;
import net.blockomorph.utils.SavedBlock;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;

public class ScrollerManager<T> {
	private static final ResourceLocation SCROLLER_SPRITE = GuiUtils.vanillaRes("container/creative_inventory/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = GuiUtils.vanillaRes("container/creative_inventory/scroller_disabled");
	@Nullable private List<T> list;
	private final IntSupplier barX;
	private final IntSupplier barY;
	private final int row;
	private final int column;
	private final int barHeight;
	private final List<T> renderable;
	protected float scrollOffset = 0; // 0 - 100 %
	private boolean scrollWork;

	public ScrollerManager(IntSupplier barX, IntSupplier barY, int height, int row, int column, List<T> renderable) {
		this.renderable = renderable;
		this.barHeight = height;
		this.barX = barX;
		this.barY = barY;
		this.row = row;
		this.column = column;
	}

	public boolean canScroll() {
		if (this.list == null) return false;
		return this.list.size() > this.row * this.column;
	}

	public void renderScroller(GuiUtils gui) {
		gui.getGuiGraphics().blitSprite(RenderType::guiTextured, this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE, this.barX.getAsInt(), this.barY.getAsInt() + (int)((float)(this.barHeight - 15) * this.scrollOffset), 12, 15);
	}

	public void setMainList(@Nullable List<T> main) {
		this.list = main;
	}

	public void setScrollOffset(float i) {
		this.scrollOffset = Math.clamp(i, 0f, 1f);
	}

	public float getScrollerOffset() {
		return this.scrollOffset;
	}

	protected int calculateRowCount() {
		return Mth.positiveCeilDiv(this.list != null ? this.list.size() : 0, 4) - 4;
	}

	protected int getRowIndexForScroll(float f) {
		return Math.max((int)((double)(f * (float)this.calculateRowCount()) + (double)0.5F), 0);
	}

	public boolean mouseClicked(double x, double y) {
		if (x > this.barY.getAsInt() + 157 && x < this.barX.getAsInt() + 157 + 13 && y > this.barY.getAsInt() && y < this.barY.getAsInt() + this.barHeight) {
			this.scrollWork = this.canScroll();
			return true;
		}
		return false;
	}

	public boolean mouseDragged(double mouseY) {
		if (this.scrollWork) {
			float scroll = ((float)mouseY - (float)this.barY.getAsInt() - 7.5F) / ((float) this.barHeight - 15.0F);
			this.scrollOffset = Mth.clamp(scroll, 0.0F, 1.0F);
			this.refreshList();
			return true;
		}
		return false;
	}

	public void disableScrollWork() {
		this.scrollWork = false;
	}

	public boolean mouseScrolled(double yScrolled) {
		if (!this.canScroll()) return false;
		this.scrollOffset = Mth.clamp(this.scrollOffset - (float)(yScrolled / (double)this.calculateRowCount()), 0.0F, 1.0F);
		this.refreshList();
		return true;
	}

	public void refreshList() {
		this.renderable.clear();
		if (this.list != null && !this.list.isEmpty()) {
			int i = this.getRowIndexForScroll(this.scrollOffset);
			int startIndex = i * this.row;

			for (int j = 0; j < this.column; ++j) {
				for (int k = 0; k < this.row; ++k) {
					int index = k + j * this.row + startIndex;

					if (index < this.list.size()) {
						this.renderable.add(this.list.get(index));
					} else break;
				}
			}
		}
	}
}
