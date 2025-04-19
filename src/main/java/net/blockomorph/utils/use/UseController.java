package net.blockomorph.utils.use;

import net.blockomorph.Blockomorph;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.MultiBlockLevel;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.accessors.BlockEntityAccessor;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.accessors.MenuAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UseController {
    private final PlayerAccessor pl;
    private final Player owner;
    private final BlockPos offset;
    private BlockState blockState;
    private BlockEntity blockEntity;
    private BlockEntityTicker ticker;
    private boolean valid = true;

    public UseController(PlayerAccessor pl, BlockPos offset, BlockState state) {
        this.pl = pl;
        this.owner = (Player) pl;
        this.offset = offset;
        this.blockState = state;
        this.initBlockEntity();
        this.initTicker();
    }

    public UseController changeBlockState(BlockState state) {
        if (state.getBlock() == this.blockState.getBlock()) {
            this.blockState = state;
            if (this.blockEntity != null) {
                this.blockEntity.setBlockState(state);
            }
            return this;
        }
        this.valid = false;
        return new UseController(this.pl, this.offset, state);
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public Player getOwner() {
        return this.owner;
    }

    public PlayerAccessor getPl() {
        return this.pl;
    }

    private void initBlockEntity() {
        if (this.blockState.getBlock() instanceof EntityBlock ent) {
            this.blockEntity = ent.newBlockEntity(this.offset, this.blockState);
            if (blockEntity != null) {
                ((BlockEntityAccessor) this.blockEntity).setUseController(this);
                UseLevel lv = new UseLevel(this.owner.level(), owner.level().isClientSide, this);
                this.blockEntity.setLevel(lv);
            }
        }
    }

    private void initTicker() {
        if (this.blockState.getBlock() instanceof EntityBlock ent && blockEntity != null) {
            this.ticker = ent.getTicker(blockEntity.getLevel(), this.blockState, blockEntity.getType());
        }
    }

    public InteractionResult use(Player clicker, BlockHitResult hiter, InteractionHand hand) {
        ((BlockPosAccessor)hiter.getBlockPos()).setUseController(this);
        UseLevel lv = new UseLevel(clicker.level(), clicker.level().isClientSide, this);
        ItemStack itemstack = clicker.getItemInHand(hand);
        if (this.owner.isSpectator()) {
            if (this.owner.level().isClientSide)
                return InteractionResult.SUCCESS;
            MenuProvider menuprovider = this.blockState.getMenuProvider(lv, hiter.getBlockPos());
            if (menuprovider != null) {
                clicker.openMenu(menuprovider);
                this.ejectChanges(lv, clicker);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }
        UseOnContext ctx = new UseOnContext(lv, clicker, hand, itemstack, hiter);
        InteractionResult result = itemstack.onItemUseFirst(ctx);
        if (result != InteractionResult.PASS) {
            this.ejectChanges(lv, clicker);
            return result;
        }
        InteractionResult interactionresult = this.blockState.use(lv, clicker, hand, hiter);
        if (interactionresult.consumesAction()) {
            this.ejectChanges(lv, clicker);
            return interactionresult;
        }
        InteractionResult interactionresult1;
        if (this.owner.isCreative()) {
            int i = itemstack.getCount();
            interactionresult1 = itemstack.useOn(ctx);
            itemstack.setCount(i);
        } else {
            interactionresult1 = itemstack.useOn(ctx);
        }
        this.ejectChanges(lv, clicker);
        return interactionresult1;
    }

    private void ejectChanges(UseLevel lv, @Nullable Player clicker) {
        if (!this.owner.level().isClientSide) {
            if (clicker != null && clicker.containerMenu instanceof MenuAccessor acc && clicker.containerMenu != clicker.inventoryMenu) {
                acc.boundToPlayer(this);
            }
            HashMap<BlockPos, SavedBlock> bls = new HashMap<>();
            HashMap<BlockPos, BlockState> blocksMain = new HashMap<>(lv.getBlocks());
            if (this.blockEntity != null) {
                if (this.blockEntity.getLevel() instanceof UseLevel level) {
                    blocksMain.putAll(level.getBlocks());
                    level.getBlocks().clear();
                }
            }
            for (Map.Entry<BlockPos, BlockState> blocks : blocksMain.entrySet()) {
                BlockPos pos = blocks.getKey();
                BlockState state = blocks.getValue();
                bls.put(pos, new SavedBlock(state, new CompoundTag(), ""));
            }
            this.pl.enableBlockOverrides(bls);
        }
    }

    public void checkChanges() {
        if (this.blockEntity != null) {
            this.ejectChanges((UseLevel) this.blockEntity.getLevel(), null);
            this.pl.saveBlockEntities();
        }
    }

    public UseController loadTag(CompoundTag tag) {
        if (this.blockEntity != null) {
            try {
                this.blockEntity.load(tag);
            } catch (Exception ignored) {}
        }
        return this;
    }

    public UseController mergeTags(CompoundTag tag) {
        if (this.blockEntity != null) {
            try {
                CompoundTag tg = this.blockEntity.saveWithoutMetadata();
                tg.merge(tag);
                this.blockEntity.load(tg);
            } catch (Exception ignored) {}
        }
        return this;
    }

    public void tick() {
        if (this.blockEntity != null) {
            if (this.ticker != null) {
                UseLevel lv = new UseLevel(owner.level(), owner.level().isClientSide, this);
                lv.setRealBlockPosMode(true);
                try {
                    ticker.tick(lv, BlockPos.containing(this.getRealPos()), this.blockState, this.blockEntity);
                } catch (Exception e) {
                    this.ticker = null;
                    Blockomorph.LOGGER.error(
                            "An unexpected exception occurred while ticking a block entity in a transformed player with username " +
                                    this.owner.getName() +
                                    ": ",
                            e
                    );
                }
                this.ejectChanges(lv, null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UseController that)) return false;
        return this.owner.getUUID().equals(that.getOwner().getUUID()) && Objects.equals(getOffset(), that.getOffset());
    }

    public boolean isValid() {
        return this.owner.isAlive() && valid;
    }

    public void setInvalid() {
        this.valid = false;
    }

    public Vec3 getRealPos() {
        return MorphUtils.getCetneredRealBlockPos(this.pl, this.offset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOwner(), getOffset(), getBlockState());
    }
}
