package net.blockomorph.mixins.main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StairBlock.class)
public class TestChair extends Block {
	public TestChair(Properties p_49795_) {
		super(p_49795_);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, BlockHitResult p_60508_) {
		Pig pig = new Pig(EntityType.PIG, p_60504_);
		pig.setPos(p_60505_.getCenter());
		pig.setNoAi(true);
		pig.setNoGravity(true);
		pig.noPhysics = true;
		p_60504_.addFreshEntity(pig);
		p_60506_.startRiding(pig);
		return InteractionResult.SUCCESS;
	}
}
