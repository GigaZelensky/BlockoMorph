package net.blockomorph.mixins.main.client.graphic;

import net.blockomorph.screens.utils.ListenerEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.BiFunction;

@Mixin(value = EditBox.class, priority = 20000)
public abstract class EditboxMixin extends AbstractWidget {
	private String tempValue;
	@Shadow @Final private Font font;
	@Shadow private BiFunction<String, Integer, FormattedCharSequence> formatter;
	@Shadow private int displayPos;
	@Shadow private boolean bordered;
	@Shadow private boolean isEditable;
	@Shadow private int textColor;
	@Shadow private int textColorUneditable;

	public EditboxMixin(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	@ModifyVariable(ordinal = 1, method = "renderWidget", at = @At("STORE"))
	private String rejectShadowRendering(String orig) {
		if ((Object)this instanceof ListenerEditBox box) {
			if (box.shadowDisabled()) {
				this.tempValue = orig;
				return "";
			}
		}
		return orig;
	}

	@ModifyVariable(ordinal = 6, method = "renderWidget", at = @At("STORE"))
	private int renderText(int o, GuiGraphics gui) {
		if ((Object)this instanceof ListenerEditBox box) {
			if (box.shadowDisabled()) {
				int y = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
				return gui.drawString(this.font, this.formatter.apply(this.tempValue, this.displayPos), this.bordered ? this.getX() + 4 : this.getX(), y, this.isEditable ? this.textColor : this.textColorUneditable);
			}
		}
		return o;
	}
}
