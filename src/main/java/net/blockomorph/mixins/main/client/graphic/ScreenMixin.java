package net.blockomorph.mixins.main.client.graphic;

import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.accessors.ScreenAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin implements ScreenAccessor {
	private Button done;
	@Shadow protected Font font;
	@Shadow public int width;
	@Shadow public int height;
	@Unique private final GuiUtils gui = new GuiUtils();
	private Runnable returnable;

	public void setReturnable(Runnable doing) {
		this.returnable = doing;
		if (doing != null) {
			this.done = Button.builder(CommonComponents.GUI_DONE, b -> doing.run()).build();
		} else this.done = null;
	}

	public Runnable getReturnable() {
		return this.returnable;
	}

	public Button getReturnButton() {
		return this.done;
	}

	@Inject(method = "renderWithTooltip", at = @At("TAIL"))
	public final void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.returnable != null) {
			this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, delta);
			this.gui.renderInDepthIfNeededAfterBlockRendering(() -> {
				this.done.setPosition(this.width / 2 - this.done.getWidth()/2, this.height - 30);
				this.done.render(guiGraphics, mouseX, mouseY, delta);
			});
		}
	}
}
