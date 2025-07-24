package net.blockomorph.screens.morphConfig;

import net.blockomorph.network.ServerBoundBlockMorphPacket;
import net.blockomorph.screens.morph.MorphScreen;
import net.blockomorph.screens.morphConfig.widget.BlockStatePropsRenderer;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MorphConfigScreen extends Screen {
	private static final ResourceLocation EXIT_TABS_SPRITE = GuiUtils.res("textures/screens/exit_tabs.png");
	private BlockStatePropsRenderer propertiesRenderer;
	private final GuiUtils gui = new GuiUtils();
	private static final ResourceLocation MENU_LOCATION = GuiUtils.res("textures/screens/morph_config_gui.png");
	public final int imageLength = 176;
	public final int imageHeight = 166;
	protected PlayerAccessor player;
	protected int leftPos;
	protected int topPos;
	private final boolean needUpperTabs;

	public MorphConfigScreen(boolean needUpperTabs) {
		super(Component.literal("morph_config_screen"));
		this.player = PlayerAccessor.of(GuiUtils.MC.player);
		this.needUpperTabs = needUpperTabs;
	}

	@Override
	public void tick() {
		this.player = PlayerAccessor.of(GuiUtils.MC.player);
	}

	private BlockState getState() {
		return this.player.getBlockState(InPlayerBlockPos.ZERO);
	}

	@Nullable
	private BlockEntity getBE() {
		return this.player.getBlockEntity(InPlayerBlockPos.ZERO);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		this.gui.setGuiGraphics(guiGraphics, this.font, mouseX, mouseY, tick);
		super.render(guiGraphics, mouseX, mouseY, tick);
		this.gui.renderBlockInGui(this.getState(), this.getBE(), this.leftPos + 45.5f, this.topPos + 76.5f, 36f);
		this.gui.renderAdditionalOnBlock(this.getState(), this.leftPos + 32, this.topPos + 34, 72f);
		this.propertiesRenderer.render(this.gui);
		if (GuiUtils.isMouseOver(this.leftPos + 15, this.topPos + 18, this.leftPos + 75, this.topPos + 79, mouseX, mouseY)) {
			this.gui.renderTooltip(this.getState().getBlock().getName(), mouseX, mouseY);
		}
	}

	private void renderStrings() {
		String blockName = this.getState().getBlock().getName().getString();
		if (this.font.width(blockName) > 81) {
			blockName = this.font.plainSubstrByWidth(blockName, 77) + "...";
		}
		this.gui.drawString(Component.literal(blockName), this.leftPos + 6, this.topPos + 6, 4210752, false);
		this.gui.drawString(Component.literal("BlockState"), this.leftPos + 100, this.topPos + 15, 4210752, false);
		this.gui.drawString(Component.literal("NBT"), this.leftPos + 9, this.topPos + 130, 4210752, false);
		this.gui.drawString(Component.translatable("gui.blockomorph.save"), this.leftPos + 13, this.topPos + 87, 4210752, false);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float tick) {
		super.renderBackground(guiGraphics, mouseX, mouseY, tick);
		this.gui.blitMonoImage(MENU_LOCATION, this.leftPos, this.topPos, this.imageLength, this.imageHeight);
		if (this.needUpperTabs)
			this.gui.blit(EXIT_TABS_SPRITE, this.leftPos + 4, this.topPos - 19, 0, 23, 80, 22, 80, 46);
		this.renderStrings();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int type) {
		if (this.needUpperTabs && GuiUtils.isMouseOver(this.leftPos + 41, this.topPos - 19, this.leftPos + 84, this.topPos + 3, mouseX, mouseY)) {
			GuiUtils.MC.setScreen(new MorphScreen());
			return true;
		} else if (this.propertiesRenderer.mouseClicked(mouseX, mouseY, type)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, type);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xWheelOffset, double yWheelOffset) {
		if (this.propertiesRenderer.mouseScrolled(mouseX, mouseY, yWheelOffset)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, xWheelOffset, yWheelOffset);
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageLength) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		this.propertiesRenderer = new BlockStatePropsRenderer(this.leftPos + 93, this.topPos + 24, 5, this::getState, newState -> {
			MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(newState, null));
		});
	}
}
