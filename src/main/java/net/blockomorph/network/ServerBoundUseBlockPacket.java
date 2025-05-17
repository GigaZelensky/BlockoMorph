package net.blockomorph.network;

import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class ServerBoundUseBlockPacket implements BlockMorphPacket {
    public static final String ID = "server_bound_use_block_packet";
    BlockHitResult hit;
    InteractionHand hand;
    int playerId;

    public ServerBoundUseBlockPacket(BlockHitResult bl, int id, InteractionHand h) {
        this.hit = bl;
        this.hand = h;
        this.playerId = id;
    }

    public ServerBoundUseBlockPacket(FriendlyByteBuf buffer) {
        this.hit = buffer.readBlockHitResult();
        this.playerId = buffer.readInt();
        this.hand = buffer.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockHitResult(this.hit);
        buffer.writeInt(this.playerId);
        buffer.writeEnum(this.hand);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void handle(Player player) {
        Entity ent = player.level().getEntity(this.playerId);
        double reach = player.getBlockReach() + 3;
        if (ent instanceof PlayerAccessor pl && player.getEyePosition().distanceToSqr(MorphUtils.getCetneredRealBlockPos(pl, hit.getBlockPos())) < reach * reach) {
            if (pl.clickPlayer(player, hit, hand).shouldSwing()) player.swing(hand, true);
        }
    }
}
