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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
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
    private Level useLevel;
    private Level tickingLevel;
    //client only
    private ModelData data;

    public UseController(PlayerAccessor pl, BlockPos offset, BlockState state) {
        this.pl = pl;
        this.owner = (Player) pl;
        this.offset = offset;
        this.blockState = state;
        ((BlockPosAccessor)this.offset).setUseController(this);
        this.doChangeDimension();
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

    public Level getUseLevel() {
        return this.useLevel;
    }

    public void updateModelData(ModelData dat) {
        this.data = dat;
    }

    public ModelData getData() {
        if (this.data == null) return ModelData.EMPTY;
        return this.data;
    }

    private void initBlockEntity() {
        if (this.blockState.getBlock() instanceof EntityBlock ent) {
            this.blockEntity = ent.newBlockEntity(this.offset, this.blockState);
            if (blockEntity != null) {
                ((BlockEntityAccessor) this.blockEntity).setUseController(this);
                this.blockEntity.setLevel(this.useLevel);
            }
        }
    }

    private void initTicker() {
        if (this.blockState.getBlock() instanceof EntityBlock ent && blockEntity != null) {
            this.ticker = ent.getTicker(this.tickingLevel, this.blockState, blockEntity.getType());
        }
    }

    public InteractionResult use(Player clicker, BlockHitResult hiter, InteractionHand hand) {
        ((BlockPosAccessor)hiter.getBlockPos()).setUseController(this);
        Level lv = this.useLevel;
        ItemStack itemstack = clicker.getItemInHand(hand);
        Blockomorph.LOGGER.info("test!");
        if (clicker.isSpectator()) {
            if (this.owner.level().isClientSide)
                return InteractionResult.SUCCESS;
            MenuProvider menuprovider = this.blockState.getMenuProvider(lv, hiter.getBlockPos());
            if (menuprovider != null) {
                clicker.openMenu(menuprovider);
                this.boundMenu(clicker);
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
        if (clicker.isCreative()) {
            int i = itemstack.getCount();
            interactionresult1 = itemstack.useOn(ctx);
            itemstack.setCount(i);
        } else {
            interactionresult1 = itemstack.useOn(ctx);
        }
        this.ejectChanges(lv, clicker);
        return interactionresult1;
    }

    private void boundMenu(Player clicker) {
        if (clicker != null && clicker.containerMenu instanceof MenuAccessor acc && clicker.containerMenu != clicker.inventoryMenu) {
            acc.boundToPlayer(this);
        }
    }

    private void ejectChanges(Level lv, @Nullable Player clicker) {
        if (lv instanceof UseServerLevel lv2) {
            this.boundMenu(clicker);
            HashMap<BlockPos, SavedBlock> bls = new HashMap<>();
            HashMap<BlockPos, BlockState> blocksMain = new HashMap<>(lv2.getBlocks());
            lv2.getBlocks().clear();
            for (Map.Entry<BlockPos, BlockState> blocks : blocksMain.entrySet()) {
                BlockPos pos = blocks.getKey();
                BlockState state = blocks.getValue();
                bls.put(pos, new SavedBlock(state, new CompoundTag(), ""));
            }
            this.pl.enableBlockOverrides(bls);
        } else if (lv instanceof UseLevel acc) {
            acc.getBlocks().clear();
        }
    }

    public UseController loadTag(CompoundTag tag) {
        if (this.blockEntity != null) {
            try {
                this.blockEntity.load(tag);
                this.blockEntity.onLoad();
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
                this.blockEntity.onLoad();
            } catch (Exception ignored) {}
        }
        return this;
    }

    public void tick() {
        if (useLevel.dimension() != this.owner.level().dimension()) this.doChangeDimension();
        if (this.blockEntity != null) {
            if (this.ticker != null) {
                Level lv = this.tickingLevel;
                try {
                    BlockPos ps = BlockPos.containing(this.getRealPos());
                    ((BlockPosAccessor)ps).setUseController(this);
                    ticker.tick(lv, ps, this.blockState, this.blockEntity);
                } catch (Exception e) {
                    this.ticker = null;
                    Blockomorph.LOGGER.error(
                            "An unexpected exception occurred while ticking a block entity in a transformed player with username " +
                                    this.owner.getName() +
                                    ": ", e
                    );
                }
                this.ejectChanges(lv, null);
            }
        }
        if (this.useLevel instanceof UseAccessor acc) {
            if (!acc.getBlocks().isEmpty()) {
                this.ejectChanges(this.useLevel, null);
            }
        }
    }

    private void doChangeDimension() {
        this.useLevel =  UseLevel.getUseLevel(this);
        this.tickingLevel = UseLevel.getUseLevel(this);
        if (this.tickingLevel instanceof UseAccessor lv) {
            lv.setRealBlockPosMode(true);
        }
        if (this.blockEntity != null) {
            this.blockEntity.setLevel(this.useLevel);
            this.initTicker();
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
