package net.blockomorph.network;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ServerBoundBlockMorphPacket implements BlockMorphPacket {
	public static final String ID = "server_bound_block_morph_packet";
	CompoundTag tag;
	private ServerBoundBlockMorphPacket(CompoundTag nbt) {
		this.tag = nbt;
	}

	public ServerBoundBlockMorphPacket(FriendlyByteBuf buf) {
		this.tag = buf.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(this.tag);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void handle(Player player) {
		if (player instanceof PlayerAccessor mob) {
			if (tag == null) throw new IllegalArgumentException("Nbt is null!");
			if (tag.contains("fuse")) {
				mob.setTnt();
				return;
			}
			if (mob.getTnt() != null) throw new IllegalArgumentException("Attempt to morph, while your morphed into burned TNT.");
			BlockState blockstate = NbtUtils.readBlockState(player.level().holderLookup(Registries.BLOCK), tag.getCompound("BlockState").orElse(new CompoundTag()));
			CompoundTag nbt = tag.getCompound("Tags").orElse(null);
			MorphUtils.BannedBlock reason = mob.applyBlockMorph(blockstate, nbt);
			if (reason != null && reason != MorphUtils.BannedBlock.SAME) {
				throw new IllegalArgumentException(reason.reason());
			}
		}
	}

	public static ServerBoundBlockMorphPacket create(BlockState state, @Nullable CompoundTag tagMorph) {
		CompoundTag tag = new CompoundTag();
		tag.put("BlockState", NbtUtils.writeBlockState(state));
		if (tagMorph != null) tag.put("Tags", tagMorph);
		return new ServerBoundBlockMorphPacket(tag);
	}

	public static ServerBoundBlockMorphPacket fuse() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("fuse", true);
		return new ServerBoundBlockMorphPacket(tag);
	}
}
