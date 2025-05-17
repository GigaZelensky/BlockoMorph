package net.blockomorph.utils.hit;

import net.blockomorph.utils.BlockInPlayer;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class PlayerHitResult {
    private static final AABB CUBE = Shapes.block().bounds();
    private static final Predicate<Entity> PREDICATE = EntitySelector.NO_SPECTATORS
            .and((mob) -> !((mob instanceof PlayerAccessor pl) && pl.isFullActive()))
            .and(Entity::isPickable);

    @Nullable
    public static Entity getEntityLookedAt(Player player, double distance, float c) {
        MorphedPlayerHitResult hit = calculateMorphedPlayerHitResult(player, distance, c, ClipContext.Block.OUTLINE, false);
        if (hit == null) return null;
        return (Player)hit.getPlayer();
    }

    @Nullable
    public static MorphedPlayerHitResult calculateMorphedPlayerHitResult(Player looker, double distance, float timeCalapse, ClipContext.Block mode, boolean skipEntityCheck) {
        if (distance < 0) distance = looker.getBlockReach();
        Vec3 eyePosition = looker.getEyePosition();
        Vec3 lookVector = looker.getViewVector(1);
        Vec3 reachVector = eyePosition.add(lookVector.x * distance, lookVector.y * distance, lookVector.z * distance);
        return calculateMorphedPlayerHitResult(looker, eyePosition, reachVector, lookVector, mode, skipEntityCheck);
    }

    @Nullable
    public static MorphedPlayerHitResult calculateMorphedPlayerHitResult(Player looker, Vec3 eyePosition, Vec3 reachVector, Vec3 lookVector, ClipContext.Block mode, boolean skipEntityCheck) {

        double entityReach = looker.getEntityReach();
        Vec3 entityReachVector = eyePosition.add(lookVector.x * entityReach, lookVector.y * entityReach, lookVector.z * entityReach);

        AABB areaBetweenAndReachEnd = new AABB(eyePosition, reachVector);
        List<Entity> entities = looker.level().getEntities(looker, areaBetweenAndReachEnd);

        MorphedPlayerHitResult result = null;
        double distanceToPartOfBlock = Double.MAX_VALUE;

        for (Entity entity : entities) {
            if (entity instanceof PlayerAccessor mob && mob.isFullActive()) {
                for (Map.Entry<BlockPos, BlockInPlayer> block : mob.getBlocksData().entrySet()) {
                    BlockInPlayer bl = block.getValue();
                    BlockPos offset = block.getKey();

                    Vec3 offsetPosInWorld = MorphUtils.getRealBlockPos(mob, offset);
                    AABB cubeAABB = CUBE.move(offsetPosInWorld);

                    if (cubeAABB.intersects(areaBetweenAndReachEnd)) {
                        VoxelShape blockShape = mode.get(bl.getBlockState(), bl.getUseController().getUseLevel(), bl.getUseController().getOffset(), CollisionContext.empty());
                        //bl.getBlockState().getShape(bl.getUseController().getUseLevel(), offset);
                        blockShape = blockShape.move(offsetPosInWorld.x, offsetPosInWorld.y, offsetPosInWorld.z);
                        for (AABB partBlockShape : blockShape.toAabbs()) {
                            Optional<Vec3> partHitResult = partBlockShape.clip(eyePosition, reachVector);
                            if (partHitResult.isPresent()) {
                                Vec3 res = partHitResult.get();
                                double dist = eyePosition.distanceTo(res);
                                if (dist < distanceToPartOfBlock) {
                                    distanceToPartOfBlock = dist;
                                    Direction dir = getClosestHitSide(blockShape, res);
                                    if (dir != null) {
                                        Vec3 inBlockOffset = new Vec3(res.x() - cubeAABB.minX, res.y() - cubeAABB.minY, res.z() - cubeAABB.minZ);
                                        result = new MorphedPlayerHitResult(
                                                mob,
                                                res,
                                                inBlockOffset.add(offset.getX(), offset.getY(), offset.getZ()),
                                                offset,
                                                dir,
                                                inBlockOffset.x == (int)inBlockOffset.x || inBlockOffset.y == (int)inBlockOffset.y ||inBlockOffset.z == (int)inBlockOffset.z
                                        );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (result != null && checkHitSuccess(looker, distanceToPartOfBlock, eyePosition, lookVector, reachVector, entityReachVector, skipEntityCheck))
            return null;

        return result;
    }

    private static boolean checkHitSuccess(Player looker, double distanceToPartOfBlock, Vec3 eyePosition, Vec3 lookVector, Vec3 reachVector, Vec3 entityReach, boolean skipEntityCheck) {
        HitResult blockhit = looker.level().clip(new ClipContext(eyePosition, reachVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, looker));
        double entReach = looker.getEntityReach();
        AABB aabb = looker.getBoundingBox().expandTowards(lookVector.scale(entReach)).inflate(1.0d);

        if (blockhit.getType() != HitResult.Type.MISS) {
            if (eyePosition.distanceTo(blockhit.getLocation()) < distanceToPartOfBlock) {
                return true;
            }
        }
        if (skipEntityCheck)
            return false;
        EntityHitResult entityhit = ProjectileUtil.getEntityHitResult(looker, eyePosition, entityReach, aabb, PREDICATE, entReach * entReach);
        return entityhit != null && eyePosition.distanceTo(entityhit.getLocation()) < distanceToPartOfBlock;
    }

    private static Direction calculateHitDirection(Vec3 hitVec, AABB boundingBox) {
        double xDist = Math.min(Math.abs(hitVec.x - boundingBox.minX), Math.abs(hitVec.x - boundingBox.maxX));
        double yDist = Math.min(Math.abs(hitVec.y - boundingBox.minY), Math.abs(hitVec.y - boundingBox.maxY));
        double zDist = Math.min(Math.abs(hitVec.z - boundingBox.minZ), Math.abs(hitVec.z - boundingBox.maxZ));

        if (xDist < yDist && xDist < zDist) {
            return hitVec.x < boundingBox.getCenter().x ? Direction.WEST : Direction.EAST;
        } else if (yDist < xDist && yDist < zDist) {
            return hitVec.y < boundingBox.getCenter().y ? Direction.DOWN : Direction.UP;
        } else {
            return hitVec.z < boundingBox.getCenter().z ? Direction.NORTH : Direction.SOUTH;
        }
    }

    @Nullable
    public static Direction getClosestHitSide(VoxelShape voxelShape, Vec3 hitPosition) {
        for (AABB aabb : voxelShape.toAabbs()) {
            if (containsAABB(aabb, hitPosition)) {
                return calculateHitDirection(hitPosition, aabb);
            }
        }

        return null;
    }

    private static boolean containsAABB(AABB ab, Vec3 tr) {
        double tolerance = 1.0E-7;
        double d = tr.x;
        double e = tr.y;
        double f = tr.z;
        return (d >= ab.minX - tolerance && d <= ab.maxX + tolerance) &&
                (e >= ab.minY - tolerance && e <= ab.maxY + tolerance) &&
                (f >= ab.minZ - tolerance && f <= ab.maxZ + tolerance);
    }
}
