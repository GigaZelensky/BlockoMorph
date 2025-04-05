package net.blockomorph.network;

import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.SavedBlock;
import net.blockomorph.utils.use.fix.BedController;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;

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
        Entity ent = player.level().getEntity(this.id);
        if (ent != null) {
            player.startRiding(ent, true);
        }
        /*HashMap<BlockPos, SavedBlock> blocks = new HashMap<>();
        blocks.put(new BlockPos(0, 1, 0), new SavedBlock(Blocks.COBBLESTONE.defaultBlockState(), new CompoundTag(), ""));
        blocks.put(new BlockPos(0, 2, 0), new SavedBlock(Blocks.TORCH.defaultBlockState(), new CompoundTag(), ""));
        blocks.put(new BlockPos(1, 0, 0), new SavedBlock(Blocks.CHEST.defaultBlockState(), new CompoundTag(), ""));
        //blocks.put(BlockPos.ZERO, new SavedBlock(Blocks.COBBLESTONE.defaultBlockState(), new CompoundTag(), ""));
        ((PlayerAccessor)player).enableBlockOverrides(blocks);
        //player.setPose(Pose.SLEEPING);*/

    }
}
