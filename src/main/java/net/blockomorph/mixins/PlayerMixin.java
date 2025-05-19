package net.blockomorph.mixins;

import net.blockomorph.Blockomorph;
import net.blockomorph.network.blockFix.ClientBoundBlockEventPacket;
import net.blockomorph.screens.BlockMorphConfigScreen;
import net.blockomorph.utils.*;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.accessors.MenuAccessor;
import net.blockomorph.utils.tnt.TntHandler;
import net.blockomorph.utils.use.UseController;
import net.blockomorph.utils.use.fix.BedController;
import net.blockomorph.utils.use.fix.ChairController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerAccessor {
	@Shadow public AbstractContainerMenu containerMenu;
	@Shadow public abstract void closeContainer();
	@Shadow @Final public InventoryMenu inventoryMenu;
	@Shadow public abstract int getSleepTimer();

	private static final EntityDataAccessor<CompoundTag> DATA_BlockMorph = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<CompoundTag> BRAKE_PROGRESS = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<CompoundTag> BED_DATA = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private static final EntityDataAccessor<CompoundTag> SIT_DATA = SynchedEntityData.defineId(Player.class, EntityDataSerializers.COMPOUND_TAG);
	private final TntHandler TNT_HANDLER = new TntHandler(this, this.entityData, BRAKE_PROGRESS);
	private final HitBoxCalculator HITBOX_HANDLER = new HitBoxCalculator(this);
	private final BedController BED_CONTROLLER = new BedController(this, this.entityData, BED_DATA);
	private final ChairController CHAIR_CONTROLLER = new ChairController(this, this.entityData, SIT_DATA);
	private final ConcurrentHashMap<BlockPos, BlockInPlayer> blocks = new ConcurrentHashMap<>() {
		@Override
		public void clear() {
			for (BlockInPlayer bl : this.values()) {
				bl.getUseController().setInvalid();
			}
			super.clear();
		}

		@Override
		public BlockInPlayer put(@NotNull BlockPos key, @NotNull BlockInPlayer value) {
			BlockInPlayer bl = this.get(key);
			if (bl != null) {
				UseController ctr = bl.getUseController();
				if (ctr != value.getUseController()) {
					ctr.setInvalid();
				}
			}
			return super.put(key, value);
		}
	};
	private boolean readyForDestroy = true;

	@Inject(method = "defineSynchedData", at = @At("TAIL"), cancellable = true)
	protected void defineSynchedData(CallbackInfo ci) {
		this.entityData.define(DATA_BlockMorph, this.getEmptyData());
		this.entityData.define(BRAKE_PROGRESS, new CompoundTag());
		this.entityData.define(BED_DATA, new CompoundTag());
		this.entityData.define(SIT_DATA, new CompoundTag());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains("BlockoMorph")) {
			CompoundTag tg = tag.getCompound("BlockoMorph");
			tg.putInt("UpdateFlag", 1);
			this.entityData.set(DATA_BlockMorph, tg, true);
		} else if (tag.contains("BlockMorph")) { //data fixer
			this.oldDataHandle(tag.getCompound("BlockMorph"));
		} else {
			this.entityData.set(DATA_BlockMorph, this.getEmptyData(), true);
		}
	}

	private void oldDataHandle(CompoundTag tag) {
		CompoundTag tg = tag.getCompound("Tags");
		CompoundTag empty = this.getEmptyData();
		CompoundTag block = empty.getCompound("Blocks").getCompound("0 0 0");
		block.put("BlockState", tag.getCompound("BlockState"));
		block.put("Tags", tg);
		empty.putInt("UpdateFlag", 2);
		this.entityData.set(DATA_BlockMorph, empty, true);
	}

	private CompoundTag getEmptyData() {
		CompoundTag morphblocktag = new CompoundTag();
		CompoundTag ZERO = new CompoundTag();
		CompoundTag elem = new CompoundTag();
		elem.put("BlockState", NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState()));
		ZERO.put("0 0 0", elem);
		morphblocktag.put("Blocks", ZERO);
		morphblocktag.putInt("UpdateFlag", 0);
		return morphblocktag;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
		this.saveBlockEntities();
		tag.put("BlockoMorph", this.entityData.get(DATA_BlockMorph));
	}

	public void saveBlockEntities() {
		if (this.level().isClientSide) return;
		CompoundTag main = this.entityData.get(DATA_BlockMorph).copy();
		main.putInt("UpdateFlag", 1);
		CompoundTag storage = main.getCompound("Blocks");
		for (Map.Entry<BlockPos, BlockInPlayer> bls : this.blocks.entrySet()) {
			CompoundTag entry = storage.getCompound(MorphUtils.getBlockPos(bls.getKey()));
			BlockState state = bls.getValue().getBlockState();
			if (state.getBlock() instanceof EntityBlock) {
				BlockEntity blockEntity = bls.getValue().getUseController().getBlockEntity();
				if (blockEntity != null) {
					entry.put("Tags", blockEntity.saveWithoutMetadata());
				} else {
					entry.remove("Tags");
				}
			} else {
				entry.remove("Tags");
			}
		}
		this.entityData.set(DATA_BlockMorph, main, true);
	}

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void attack(Entity entity, CallbackInfo ci) {
		if (entity instanceof PlayerAccessor pl && pl.isActive())
			ci.cancel();
	}

	@Inject(method = "tick", at = @At("TAIL"), cancellable = true)
	public void tick(CallbackInfo ci) {
        for (BlockInPlayer block : this.blocks.values()) {
			block.tick();
		}
		TNT_HANDLER.tick();
		if (this.isPlayerContainerInvalid()) {
			this.closeContainer();
			this.containerMenu = this.inventoryMenu;
		}
		BED_CONTROLLER.tick();
		CHAIR_CONTROLLER.tick();
	}

	private boolean isPlayerContainerInvalid() {
		if (this.containerMenu instanceof MenuAccessor accessor && !this.level().isClientSide) {
			UseController ctr = accessor.getPlayer();
			if (ctr != null) {
				if (!ctr.isValid()) {
					return true;
				} else {
                    return this.distanceToSqr(ctr.getRealPos()) >= 64.0D;
				}
			}
		}
		return false;
	}

	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	public void causeFallDamage(float f, float g, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
		if (this.isActive()) {
			cir.cancel();
			if (!(this.getBlockState().getBlock() instanceof AnvilBlock)) {
				return;
			}
			int i = Mth.ceil(f - 1.0f);
			if (i < 0) {
				return;
			}
			Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
			DamageSource damageSource22 = this.damageSources().anvil(this);
			float h = Math.min(Mth.floor((float) i * 2), 40);
			this.level().getEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.hurt(damageSource22, h));
		}
	}

	public void applyBlockMorph(BlockState state, CompoundTag tag) {
		CompoundTag mainTag = this.entityData.get(DATA_BlockMorph);
		mainTag = mainTag.copy();
		mainTag.putInt("UpdateFlag", 1);
		if (state.getBlock() != this.getBlockState().getBlock()) {
			mainTag.putInt("UpdateFlag", 2);
			CompoundTag elements = new CompoundTag();
			MultiBlockLevel lv = new MultiBlockLevel(this.level(), false);
			state.getBlock().setPlacedBy(lv, BlockPos.ZERO, state, this.player(), new ItemStack(state.getBlock()));
			for (Map.Entry<BlockPos, BlockState> bls : lv.getBlocks().entrySet()) {
				CompoundTag tg = new CompoundTag();
				tg.put("BlockState", NbtUtils.writeBlockState(bls.getValue()));
				tg.put("Tags", new CompoundTag());
				elements.put(MorphUtils.getBlockPos(bls.getKey()), tg);
			}
			mainTag.put("Blocks", elements);
		}
		CompoundTag morphblocktag = mainTag.getCompound("Blocks").getCompound("0 0 0");
		if (state.getBlock() instanceof EntityBlock bl) {
			CompoundTag blockEntityTag;
			try {
				BlockEntity ent = bl.newBlockEntity(this.blockPosition(), state);
				if (ent != null) {
					blockEntityTag = ent.saveWithoutMetadata();
				} else {
					blockEntityTag = new CompoundTag();
				}
				if (tag != null) {
                    blockEntityTag.merge(tag);
				}
			} catch (Exception e) {
				Blockomorph.LOGGER.warn("When receiving original tags from the block entity of the player " + this + " an error occurred: ", e);
				blockEntityTag = new CompoundTag();
				blockEntityTag.merge(tag);
			}
			morphblocktag.put("Tags", blockEntityTag);
		} else {
			morphblocktag.put("Tags", new CompoundTag());
		}
		morphblocktag.put("BlockState", NbtUtils.writeBlockState(state));
		mainTag.getCompound("Blocks").put("0 0 0", morphblocktag);
		this.entityData.set(DATA_BlockMorph, mainTag, true);
		TNT_HANDLER.setFuse(-1);
		TNT_HANDLER.setTnt((PrimedTnt) null);
		HashMap<BlockPos, BlockState> runUpdate = new HashMap<>();
		runUpdate.put(BlockPos.ZERO, state);
		this.neightbourUpdate(runUpdate);
	}

	public void enableBlockOverrides(HashMap<BlockPos, SavedBlock> blocks) {
		this.enableBlockOverrides(blocks, true);
	}

	private void enableBlockOverrides(HashMap<BlockPos, SavedBlock> blocks, boolean update) {
		if (!this.isFullActive() || blocks.isEmpty()) return;
		if (blocks.containsKey(BlockPos.ZERO)) {
			if (blocks.get(BlockPos.ZERO).getState().getBlock() == Blocks.AIR) {
				this.applyBlockMorph(Blocks.AIR.defaultBlockState(), new CompoundTag());
				return;
			}
		}

		CompoundTag morphblocktag = this.entityData.get(DATA_BlockMorph);
		morphblocktag = morphblocktag.copy();
		morphblocktag.putInt("UpdateFlag", 0);
		CompoundTag elemets = new CompoundTag();

		HashMap<BlockPos, BlockState> updatingBlocks = new HashMap<>();

		for (Map.Entry<BlockPos, SavedBlock> bl : blocks.entrySet()) {
			SavedBlock block = bl.getValue();
			if (block.getState() != null && block.getTag() != null) {
				CompoundTag tg = new CompoundTag();
				tg.put("BlockState", NbtUtils.writeBlockState(block.getState()));
				tg.put("Tags", block.getTag());
				elemets.put(MorphUtils.getBlockPos(bl.getKey()), tg);
			}
			updatingBlocks.put(bl.getKey(), block.getState());
		}
		CompoundTag now = new CompoundTag();
		now.put("Blocks", elemets);
		morphblocktag.merge(now);
		this.entityData.set(DATA_BlockMorph, morphblocktag, true);
		this.saveBlockEntities();
		if (update)
			this.neightbourUpdate(updatingBlocks);
	}

	private void neightbourUpdate(HashMap<BlockPos, BlockState> blocks) {
		HashMap<BlockPos, BlockState> updatedBlocks = new HashMap<>();
		for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
			BlockPos offset = block.getKey();
			this.updateBlock(offset, blocks, updatedBlocks);
		}
		HashMap<BlockPos, SavedBlock> finalUpdated = new HashMap<>();
		for (Map.Entry<BlockPos, BlockState> block : updatedBlocks.entrySet()) {
			finalUpdated.put(block.getKey(), new SavedBlock(block.getValue(), new CompoundTag(), ""));
		}
		this.enableBlockOverrides(finalUpdated, false);
	}

	private void updateBlock(BlockPos pos, HashMap<BlockPos, BlockState> banned, HashMap<BlockPos, BlockState> updating) {
		for (Direction updDir : Direction.values()) {
			BlockPos offsetted = pos.relative(updDir);
			if (this.getUseControllers().containsKey(offsetted) && !banned.containsKey(offsetted) && !updating.containsKey(offsetted)) {
				BlockState ctr = this.getUseControllers().get(offsetted).getBlockState();
				MultiBlockLevel lv = new MultiBlockLevel(this.level(), false);
				lv.getBlocks().putAll(this.getBlocks());
				lv.getBlocks().putAll(updating);

				BlockState ctr2 = ctr.getBlock().updateShape(ctr, updDir.getOpposite(), this.getUseControllers().get(pos).getBlockState(), lv, offsetted, pos);

				if (!ctr.equals(ctr2)) {
					updating.put(offsetted, ctr2);
					this.updateBlock(offsetted, banned, updating);
				}
			}
		}
	}

	public InteractionResult clickPlayer(Player clicker, BlockHitResult hiter, InteractionHand hand) {
		UseController ctr = this.getUseControllers().get(hiter.getBlockPos());
		if (ctr == null) return InteractionResult.FAIL;
		if (ctr.getBlockState().getBlock() instanceof TntBlock) {
			return TNT_HANDLER.clckTnt(clicker, hiter, hand);
		} else if (ctr.getBlockState().getBlock() instanceof BedBlock) {
			return PlayerAccessor.of(clicker).getBedController().clckBed(this, hiter);
		}
		return ctr.use(clicker, hiter, hand);
	}

	public PrimedTnt getTnt() {
		return TNT_HANDLER.getTnt();
	}

	public void setTnt() {
		TNT_HANDLER.setTnt();
	}

	public BedController getBedController() {
		return BED_CONTROLLER;
	}

	public ChairController getChairController() {
		return CHAIR_CONTROLLER;
	}

	public BlockState getBlockState() {
		if (this.blocks == null || this.blocks.get(BlockPos.ZERO) == null) return Blocks.AIR.defaultBlockState();
		return this.blocks.get(BlockPos.ZERO).getBlockState();
	}

	public CompoundTag getTag() {
		if (this.blocks.get(BlockPos.ZERO) == null) return new CompoundTag();
		BlockEntity blockEntity = this.blocks.get(BlockPos.ZERO).getUseController().getBlockEntity();
		if (blockEntity == null) return new CompoundTag();
		return blockEntity.saveWithoutMetadata();
	}

	public boolean isActive() {
		return this.getBlockState().getBlock() != Blocks.AIR;
	}

	public boolean isFullActive() {
		return this.isActive() && TNT_HANDLER.getTnt() == null;
	}

	public void setReady(boolean flag) {
		this.readyForDestroy = flag;
	}

	public boolean readyForDestroy() {
		return this.readyForDestroy;
	}

	public BlockPos minPos() {
		return HITBOX_HANDLER.getMinPos();
	}

	public BlockPos maxPos() {
		return HITBOX_HANDLER.getMaxPos();
	}

	public HashMap<BlockPos, BlockInPlayer> getBlocksData() {
		return new HashMap<>(this.blocks);
	}

	public HashMap<BlockPos, BlockState> getBlocks() {
		HashMap<BlockPos, BlockState> bls = new HashMap<>();
		for (Map.Entry<BlockPos, BlockInPlayer> bl : this.blocks.entrySet()) {
			bls.put(bl.getKey(), bl.getValue().getBlockState());
		}
		return bls;
	}

	public HashMap<BlockPos, UseController> getUseControllers() {
		HashMap<BlockPos, UseController> bls = new HashMap<>();
		for (Map.Entry<BlockPos, BlockInPlayer> bl : this.blocks.entrySet()) {
			bls.put(bl.getKey(), bl.getValue().getUseController());
		}
		return bls;
	}

	private int getUpdateFlag() {
		return this.entityData.get(DATA_BlockMorph).getInt("UpdateFlag");
	}

	public void removePlayer(BlockPos pos, Player pl) {
		for (BlockInPlayer br : this.blocks.values()) {
			if (br.getBlockBracker().getPos().equals(pos)) {
				br.getBlockBracker().removePlayer(pl);
				return;
			}
		}
	}

	private void resetUpdateFlag() {
		if (this.level().isClientSide) return;
		CompoundTag main = this.entityData.get(DATA_BlockMorph).copy();
		main.putInt("UpdateFlag", 0);
		this.entityData.set(DATA_BlockMorph, main);
	}

	private void updateBlocks() {

		//this.resetUpdateFlag();
		//tnt reset
		CompoundTag tg = new CompoundTag();
		tg.putInt("fuse", -1);
		this.entityData.set(BRAKE_PROGRESS, tg);

		CompoundTag blocks = this.entityData.get(DATA_BlockMorph).getCompound("Blocks").copy();
		//reset - 2
		if (this.getUpdateFlag() == 2) {
			this.blocks.clear();
			for (String key : blocks.getAllKeys()) {
				CompoundTag xyz = blocks.getCompound(key);
				BlockPos pos = MorphUtils.parseBlockPos(key);
				BlockState state = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), xyz.getCompound("BlockState"));
				CompoundTag nbt = xyz.getCompound("Tags").copy();
				this.blocks.put(pos, new BlockInPlayer(
						state,
						new BlockBracker(this, pos, state, this.entityData, BRAKE_PROGRESS),
						new UseController(this, pos, state).loadTag(nbt)
				));
			}
			if (!this.level().isClientSide)
				MorphUtils.sendAll(ClientBoundBlockEventPacket.levelEvent(this.getId(), BlockPos.ZERO, -4, 0));
			return;
		}

		//soft block sync
		if (this.getUpdateFlag() == 1) {
			for (String key : blocks.getAllKeys()) {
				UseController controller;
				BlockPos pos = MorphUtils.parseBlockPos(key);
				CompoundTag xyz = blocks.getCompound(key);
				BlockState state3 = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), xyz.getCompound("BlockState"));
				if (this.blocks.containsKey(pos)) {
					controller = this.blocks.get(pos).getUseController().changeBlockState(state3).mergeTags(xyz.getCompound("Tags"));
				} else {
					controller = new UseController(this, pos, state3).loadTag(xyz.getCompound("Tags"));
				}
				this.blocks.put(pos, new BlockInPlayer(
						state3,
						new BlockBracker(this, pos, state3, this.entityData, BRAKE_PROGRESS),
						controller
				));
			}
		}

	}

	public int getBiggestProgress() {
		int i = -1;
		try {
			for (BlockInPlayer br : this.blocks.values()) {
				int j = br.getBlockBracker().getProgress();
				if (j > i)
					i = j;
			}
		} catch (Exception ignored) {
		}
		return i;
	}

	public void addPlayer(BlockPos pos, Player pl) {
		for (BlockInPlayer br : this.blocks.values()) {
			if (br.getBlockBracker().getPos().equals(pos)) {
				br.getBlockBracker().addPlayer(pl);
				return;
			}
		}
	}

	public CompoundTag getProgress() {
		return this.entityData.get(BRAKE_PROGRESS);
	}

	@Inject(method = "getStandingEyeHeight", at = @At("HEAD"), cancellable = true)
	private void getStandingEyeHeight(Pose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
		if (this.isActive()) {
			cir.setReturnValue(HITBOX_HANDLER.getEyeHeight());
		}
	}

	@Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
	public void getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if (this.isActive()) {
			cir.setReturnValue(HITBOX_HANDLER.calculateDimensions());
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
		super.onSyncedDataUpdated(data);

		if (DATA_BlockMorph.equals(data)) {
			if (this.getUpdateFlag() == 0) {
				return;
			}
			this.updateBlocks();
			HITBOX_HANDLER.recalculatePositions();
			this.refreshDimensions();
			if (this.level().isClientSide())
				this.clientUpdate();
		} else if (BRAKE_PROGRESS.equals(data)) {
			TNT_HANDLER.onClientUpdater();
		} else if (BED_DATA.equals(data)) {
			BED_CONTROLLER.syncData();
		} else if (SIT_DATA.equals(data)) {
			CHAIR_CONTROLLER.syncData();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void clientUpdate() {
		if (Minecraft.getInstance().screen instanceof BlockMorphConfigScreen sc && Minecraft.getInstance().player == (Player)(Object)this)
			sc.morphUpdate(this.getBlockState());
	}

	@Override
	@Nullable
	public ItemStack getPickResult() {
		if (this.isActive()) {
			Item item = this.getBlockState().getBlock().asItem();
			return new ItemStack(item);
		}
		return null;
	}

	public VoxelShape getShape(BlockPos offset, @Nullable Vec3 realPos) {
		BlockInPlayer block = this.getBlocksData().get(offset);
		if (block == null) return Shapes.empty();
		UseController ctr = block.getUseController();
		VoxelShape shp = ctr.getBlockState().getCollisionShape(ctr.getUseLevel(), BlockPosAccessor.of(BlockPos.containing(ctr.getRealPos())).setUseController(ctr), CollisionContext.of(this));
		Vec3 rl;
		if (realPos != null) {
			rl = realPos;
		} else {
			rl = MorphUtils.getRealBlockPos(this, offset);
		}
		return shp.move(rl.x, rl.y, rl.z);
	}

	public VoxelShape getRenderShape(BlockPos pos) {
		boolean flag = pos.equals(BlockPos.ZERO);
		if (this.getBlocks().containsKey(pos) || flag) {
			BlockState state;
			if (flag) {
				state = this.getBlockState();
			} else {
				state = this.getBlocks().get(pos);
			}
			BlockInPlayer block = this.getBlocksData().get(pos);
			UseController ctr = block.getUseController();
			VoxelShape shape = state.getShape(ctr.getUseLevel(), BlockPosAccessor.of(BlockPos.containing(ctr.getRealPos())).setUseController(ctr), CollisionContext.of(this));
            return shape.move(pos.getX(), pos.getY(), pos.getZ());
		}
		return Shapes.empty();
	}

	@Override
	public boolean isAttackable() {
		return !this.isActive();
	}

	@Override
	public boolean skipAttackInteraction(@NotNull Entity ent) {
		return this.isActive();
	}

	@Override
	public void push(@NotNull Entity mob) {
		if (!this.isActive())
			super.push(mob);
	}

	@Override
	protected void pushEntities() {
		if (!this.isActive())
			super.pushEntities();
	}

	@Override
	public boolean canBeSeenByAnyone() {
		return !this.isActive();
	}

	@Inject(method = "getDeathSound", at = @At("HEAD"), cancellable = true)
	protected void getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
		if (this.isActive())
			cir.setReturnValue(null);
	}

	@Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
	protected void getHurtSound(DamageSource damage, CallbackInfoReturnable<SoundEvent> cir) {
		if (this.isActive())
			cir.setReturnValue(null);
	}

	@Inject(method = "getFallSounds", at = @At("HEAD"), cancellable = true)
	protected void getFallSounds(CallbackInfoReturnable<Fallsounds> cir) {
		if (this.isActive())
			cir.setReturnValue(new Fallsounds(SoundEvents.EMPTY, SoundEvents.EMPTY));
	}

	@Inject(method = "maybeBackOffFromEdge", at = @At(value = "RETURN"), cancellable = true)
	public void fixMovement(Vec3 originalVector, MoverType moverType, CallbackInfoReturnable<Vec3> cir) {
		if (this.isActive() && (moverType == MoverType.PLAYER || moverType == MoverType.SELF)) {
			MovementCalculator calculator = new MovementCalculator(this);
			calculator.calculateEnterCorrection(originalVector);
		}
	}

	@Override
	public boolean isSleeping() {
		if (BED_CONTROLLER.isWorking())
			return true;
		return super.isSleeping();
	}

	@Nullable
	public Direction getBedOrientation() {
		if (BED_CONTROLLER.isWorking()) {
			return BED_CONTROLLER.getTarget().getBlockState().getValue(HorizontalDirectionalBlock.FACING);
		}
		return super.getBedOrientation();
	}

	@Inject(method = "getSleepTimer", at = @At(value = "HEAD"), cancellable = true)
	public void fix(CallbackInfoReturnable<Integer> cir) {
		if (BED_CONTROLLER.getSleepCounter() > 0) {
			cir.setReturnValue(BED_CONTROLLER.getSleepCounter());
		}
	}

	@Redirect(method = "tick", at = @At(
			value = "INVOKE",
			target = "net/minecraft/world/entity/player/Player.isSleeping()Z",
			ordinal = 0)
	)
	private boolean redirectIsSleeping(Player player) {
		if (BED_CONTROLLER.isWorking()) {
			return false;
		} else return super.isSleeping();
	}

	@Redirect(method = "isSleepingLongEnough", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;sleepCounter:I"))
	private int redirectSleepCounter(Player instance) {
		return this.getSleepTimer();
	}

	@Override
	protected void positionRider(@NotNull Entity passanger, @NotNull MoveFunction setPosition) {
		if (this.hasPassenger(passanger) && passanger instanceof PlayerAccessor plPass) {
			BedController bd = plPass.getBedController();
			ChairController ch = plPass.getChairController();
			UseController ctr = bd.getTarget();
			if (ctr != null) {
				if (ctr.getPl() == this) {
					Vec3 bedPos = ctr.getRealPos().add(0, 0.0625, 0);
					setPosition.accept(passanger, bedPos.x, bedPos.y, bedPos.z);
					return;
				}
			}
			ctr = ch.getTarget();
			if (ctr != null && ctr.getPl() == this) {
				Vec3 chairPos = MorphUtils.getRealBlockPos(ctr.getPl(), BlockPos.ZERO).add(ch.getChairPos());
				setPosition.accept(passanger, chairPos.x, chairPos.y, chairPos.z);
				return;
			}
		}
		super.positionRider(passanger, setPosition);
	}

	/*private MultiBlockLevel LIQIUD_LEVEL;

	public MultiBlockLevel getLiquidCachedLevel() {
		if (LIQIUD_LEVEL == null && this.level().isClientSide()) {
			LIQIUD_LEVEL = new MultiBlockLevel(this.level(), true) {
				@Override
				public BlockState getBlockState(BlockPospos) {
					BlockInPlayer bl = blocks.get(pos);
					if (bl != null) return bl.getBlockState();
					return Blocks.AIR.defaultBlockState();
				}
			};
		}
		return LIQIUD_LEVEL;
	}*/

	public PlayerMixin() {
		super(null, null);
	}
}
