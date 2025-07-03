package net.blockomorph.mixins.main.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.blockomorph.utils.BlockInPlayer2;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.blockomorph.utils.coords.PlayerMorphedSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModelDataManager.class, remap = false)
public class ModelDataManagerMixin {

	@Inject(method = "requestRefresh", at = @At("HEAD"), cancellable = true)
	public void write(BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity != null) {
			InPlayerBlockPos.check(blockEntity.getBlockPos(), (pl, realPos) -> {
				BlockInPlayer2 block = pl.getBlocksData2().get(realPos);
				if (block != null) {
					block.connectModelData(blockEntity.getModelData());
				}
			}, ci::cancel, true);
		}
	}

	@Inject(method = "getAt(Lnet/minecraft/core/BlockPos;)Lnet/neoforged/neoforge/model/data/ModelData;", at = @At("HEAD"), cancellable = true)
	public void read(BlockPos pos, CallbackInfoReturnable<ModelData> cir) {
		InPlayerBlockPos.check(pos, (pl, realPos) -> {
			BlockInPlayer2 block = pl.getBlocksData2().get(realPos);
			if (block != null) {
				cir.setReturnValue(block.getModelData());
			}
		}, null, true);
	}

	@Inject(method = "getAt(Lnet/minecraft/core/SectionPos;)Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", at = @At("RETURN"), cancellable = true)
	public void readSection(SectionPos pos, CallbackInfoReturnable<Long2ObjectMap<ModelData>> cir) {
		ChunkPos chunkPos = new ChunkPos(pos.getX(), pos.getZ());
		Player player = PlayerMorphedSection.getPlayerByChunkPos(chunkPos, true);
		if (player instanceof PlayerAccessor pl) {
			Long2ObjectMap<ModelData> map = new Long2ObjectOpenHashMap<>();
			for (BlockInPlayer2 block : pl.getBlocksData2().values()) {
				map.put(block.getPos().asLong(), block.getModelData());
			}
			cir.setReturnValue(Long2ObjectMaps.unmodifiable(map));
		}
	}
}
