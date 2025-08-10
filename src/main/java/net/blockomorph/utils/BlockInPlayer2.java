package net.blockomorph.utils;

import net.blockomorph.Blockomorph;
import net.blockomorph.network.ClientBoundMorphUpdatePacket;
import net.blockomorph.utils.coords.BlockPosBounds;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class BlockInPlayer2 {
	private final InPlayerBlockPos offset;
	private final BlockPos pos;
	private final Player player;
	private BlockState blockState;
	private BlockEntity blockEntity;
	private BlockEntityTicker blockEntityTicker;
	//client only \/
	private ModelData data = ModelData.EMPTY;

	public BlockInPlayer2(PlayerAccessor pl, InPlayerBlockPos pos, BlockState state, Consumer<BlockInPlayer2> preInit) {
		this.offset = pos;
		this.pos = pos.boundedBlockPos(pl.player());
		if (this.pos == null)
			throw new IllegalArgumentException("Null BlockPos in BlockInPlayer! Player section: " + BlockPosBounds.getChunkPosForPlayer(pl.player()) +
					" Level: " + pl.player().level());
		this.player = pl.player();
		this.blockState = state;
		preInit.accept(this);
		this.initBlockEntity();
		this.initTicker();
	}

	public BlockState getBlockState() {
		return blockState;
	}

	public BlockEntity getBlockEntity() {
		return blockEntity;
	}

	public BlockPos getPos() {
		return pos;
	}

	public InPlayerBlockPos getOffset() {
		return offset;
	}

	@Nullable
	public Throwable loadNBT(CompoundTag tg) {
		if (this.blockEntity != null) {
			try {
				this.blockEntity.loadWithComponents(tg, this.player.level().registryAccess());
			} catch (Throwable e) {
				return e;
			}
		}
		return null;
	}

	public BlockInPlayer2 handleClientTag(CompoundTag tg, ClientBoundMorphUpdatePacket pkt) {
		if (this.blockEntity != null) {
			try {
				this.blockEntity.onDataPacket(pkt.getListener().getConnection(), ClientboundBlockEntityDataPacket.create(this.blockEntity, (ent, access) -> tg), this.player.registryAccess());
			} catch (Exception ignored) {}
		}
		return this;
	}

	public void onPlace(BlockState newState, BlockState oldState, int flags) {
		if (!this.player.level().isClientSide)
			newState.onPlace(this.player.level(), this.pos, oldState, (flags & 64) != 0);
	}

	public BlockInPlayer2 changeBlockState(BlockState state, int flags) {
		BlockState old = this.blockState;
		this.blockState = state;
		boolean newBlock = !old.is(state.getBlock());
		boolean bl4 = (flags & 64) != 0;
		boolean bl5 = (flags & 256) == 0;

		if (newBlock && old.hasBlockEntity()) {
			if (!this.player.level().isClientSide && bl5) {
				if (blockEntity != null) {
					blockEntity.preRemoveSideEffects(this.pos, old);
				}
			}

			this.clearBlockEntity();
		}

		if (newBlock || state.getBlock() instanceof BaseRailBlock) {
			this.checkUpdates(flags, old);
		}

		if (!this.player.level().isClientSide && (flags & 512) == 0) {
			this.onPlace(state, old, flags);
		}

		if (state.hasBlockEntity()) {
			if (this.blockEntity == null) {
				this.initBlockEntity();
			} else {
				this.blockEntity.setBlockState(state);
			}
		}

		this.initTicker();
		return this;
	}

	public void checkUpdates(int flags, BlockState old) {
		if (this.player.level() instanceof ServerLevel serverLevel) {
			boolean bl4 = (flags & 64) != 0;
			if ((flags & 1) != 0 || bl4) {
				old.affectNeighborsAfterRemoval(serverLevel, this.pos, bl4);
			}
		}
	}

	public void clearBlockEntity() {
		this.blockEntity = null;
		this.blockEntityTicker = null;
	}

	private void initBlockEntity() {
		if (this.blockState.getBlock() instanceof EntityBlock ent) {
			this.blockEntity = ent.newBlockEntity(this.pos, this.blockState);
			if (blockEntity != null) {
				this.blockEntity.setLevel(this.player.level());
				this.blockEntity.onLoad();
			}
		}
	}

	private void initTicker() {
		if (this.blockState.getBlock() instanceof EntityBlock ent && blockEntity != null) {
			this.blockEntityTicker = ent.getTicker(this.player.level(), this.blockState, blockEntity.getType());
		}
	}

	public void connectModelData(ModelData data) {
		this.data = Objects.requireNonNullElse(data, ModelData.EMPTY);
	}

	public ModelData getModelData() {
		return this.data;
	}

	public void tick() {
		if (this.blockEntity != null) {
			if (this.blockEntityTicker != null) {
				try {
					blockEntityTicker.tick(this.player.level(), this.pos, this.blockState, this.blockEntity);
				} catch (Exception e) {
					this.blockEntityTicker = null;
					MorphUtils.LOGGER.error(
							"An unexpected exception occurred while ticking a block entity in a transformed player with username " +
									this.player.getName().getString() +
									": ", e
					);
				}
			}
		}
	}

	public void animateTick() {
		if (this.player.level().isClientSide) {
			this.blockState.getBlock().animateTick(this.blockState, this.player.level(), this.pos, this.player.getRandom());
		}
	}

	private boolean needRemoveBlockEntity(BlockState old, BlockState newState) {
		return old.hasBlockEntity() && (!old.is(newState.getBlock()) || !newState.hasBlockEntity());
	}
}
