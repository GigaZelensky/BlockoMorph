package net.blockomorph.network;

import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.coords.InPlayerBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/************************************************************************************************************************
 Temporary package, does not carry any functions, created exclusively for testing, it will be removed in the next update!
 ***********************************************************************************************************************/

public class DebugPacket2 implements BlockMorphPacket {
    int id;
    public DebugPacket2(int id) {
        this.id = id;
    }
    public DebugPacket2(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
    }

    @Override
    public String getId() {
        return "db2";
    }

    @Override
    public void handle(Player player) {
        if (checkDisabled())
            return;
        //Entity ent = player.level().getEntity(this.id);
        //if (ent != null) {
            //player.startRiding(ent, true);
        //}
        /*HashMap<BlockPos, SavedBlock> blocks = new HashMap<>();
        blocks.put(new BlockPos(0, 1, 0), new SavedBlock(Blocks.COBBLESTONE.defaultBlockState(), new CompoundTag(), ""));
        blocks.put(new BlockPos(0, 2, 0), new SavedBlock(Blocks.TORCH.defaultBlockState(), new CompoundTag(), ""));
        blocks.put(new BlockPos(1, 0, 0), new SavedBlock(Blocks.CHEST.defaultBlockState(), new CompoundTag(), ""));
        //blocks.put(BlockPos.ZERO, new SavedBlock(Blocks.COBBLESTONE.defaultBlockState(), new CompoundTag(), ""));
        ((PlayerAccessor)player).enableBlockOverrides(blocks);
        //player.setPose(Pose.SLEEPING);
        if (player instanceof PlayerAccessor pl) {
            BlockEntity bl = pl.getUseControllers().get(BlockPos.ZERO).getBlockEntity();
            if (bl != null&&bl.getLevel() instanceof UseServerLevel LV2) {
                //LV2.sendBlockUpdated(bl.getBlockPos(), null, null, 0);
            }
        }*/
        //PlayerAccessor.of(player).loadBlockData(new CompoundTag());
        if (player instanceof ServerPlayer pl) {
            BlockPos pos = InPlayerBlockPos.ZERO.boundedBlockPos(pl);
            Vec3 vec = pos.getCenter();
            //pl.connection.teleport(vec.x + 10, vec.y + 1, vec.z, pl.getYRot(), pl.getXRot());
        }
    }

    protected static boolean checkDisabled() {
        return true;
    }
}
