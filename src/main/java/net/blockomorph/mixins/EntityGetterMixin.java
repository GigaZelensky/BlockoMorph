package net.blockomorph.mixins;

import com.google.common.collect.ImmutableList;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class EntityGetterMixin implements LevelAccessor, CollisionGetter {

   @Override
   public List<VoxelShape> getEntityCollisions(@Nullable Entity p_186451_, AABB p_186452_) {

   	  if (p_186452_.getSize() < 1.0E-7D) {
         return List.of();
      } else {
         Predicate<Entity> predicate = p_186451_ == null ? EntitySelector.CAN_BE_COLLIDED_WITH : 
         EntitySelector.NO_SPECTATORS.and(entity -> entity instanceof Player || p_186451_.canCollideWith(entity));
         
         List<Entity> list = this.getEntities(p_186451_, p_186452_.inflate(2.0), predicate);
         if (list.isEmpty()) {
            return List.of();
         } else {
            ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());

            for(Entity entity : list) {
               if (entity instanceof PlayerAccessor pl) {

                  AABB cube = Shapes.block().bounds().inflate(1);
                  List<VoxelShape> blocks = new ArrayList<>();
                  AABB aabbPl = p_186452_.inflate(1.0E-7);

                  if (pl.isFullActive()) {
                     for (BlockPos offset : pl.getBlocksData().keySet()) {
                        Vec3 realpos = MorphUtils.getRealBlockPos(pl, offset);
                        AABB movedCube = cube.move(realpos);
                        if (aabbPl.intersects(movedCube)) {
                           blocks.add(pl.getShape(offset, realpos));
                        }
                     }
                     for (VoxelShape shp : blocks) {
                        builder.add(shp);
                     }
                  }
               } else {
                  builder.add(Shapes.create(entity.getBoundingBox()));
               }
            }
            return builder.build();

         }
      }
   }

   @Override
   public boolean noCollision(@Nullable Entity p_45757_, AABB p_45758_) {
      for(VoxelShape voxelshape : this.getBlockCollisions(p_45757_, p_45758_)) {
         if (!voxelshape.isEmpty()) {
            return false;
         }
      }
      
      for (VoxelShape entityShape : this.getEntityCollisions(p_45757_, p_45758_)) {
         if (Shapes.joinIsNotEmpty(entityShape, Shapes.create(p_45758_), BooleanOp.AND)) {
            return false;
         }
      }

      if (p_45757_ == null) {
         return true;
      } else {
         VoxelShape voxelshape1 = this.borderCollision(p_45757_, p_45758_);
         return voxelshape1 == null || !Shapes.joinIsNotEmpty(voxelshape1, Shapes.create(p_45758_), BooleanOp.AND);
      }
   }

   @Nullable
   private VoxelShape borderCollision(Entity p_186441_, AABB p_186442_) {
      WorldBorder worldborder = this.getWorldBorder();
      return worldborder.isInsideCloseToBorder(p_186441_, p_186442_) ? worldborder.getCollisionShape() : null;
   }

   @Inject(method = "getBlockEntity", at = @At(value = "HEAD"))
   public void ridirectGetterBlockEntity(BlockPos blockPos, CallbackInfoReturnable<BlockEntity> cir) {
      UseController ctr = MorphUtils.getControllerFromPos(blockPos);
      if (ctr != null) {
         cir.setReturnValue(ctr.getBlockEntity());
      }
   }

   @Inject(method = "getBlockState", at = @At(value = "HEAD"))
   public void redirectGetterBlockState(BlockPos blockPos, CallbackInfoReturnable<BlockState> cir) {
      UseController ctr = MorphUtils.getControllerFromPos(blockPos);
      if (ctr != null) {
         cir.setReturnValue(ctr.getBlockState());
      }
   }
}
