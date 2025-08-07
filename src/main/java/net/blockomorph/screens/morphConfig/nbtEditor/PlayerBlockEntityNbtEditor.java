package net.blockomorph.screens.morphConfig.nbtEditor;

import net.blockomorph.network.ServerBoundBlockMorphPacket;
import net.blockomorph.network.ServerBoundSelfNbtRequestPacket;
import net.blockomorph.screens.utils.GuiUtils;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.nbt.CompoundTag;

public class PlayerBlockEntityNbtEditor extends NbtEditorScreen {
	private boolean init;

	private PlayerAccessor getPlayer() {
		return PlayerAccessor.of(GuiUtils.MC.player);
	}

	@Override
	protected void onTagEdited(CompoundTag tag) {
		MorphUtils.sendServer(ServerBoundBlockMorphPacket.create(this.getPlayer().getBlockState(InPlayerBlockPos.ZERO), tag));
		this.setError(null);
	}

	@Override
	protected void init() {
		super.init();
		if (!this.init) {
			MorphUtils.sendServer(new ServerBoundSelfNbtRequestPacket());
			this.init = true;
		}
	}
}
