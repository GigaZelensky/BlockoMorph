package net.blockomorph.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.blockomorph.screens.BlockMorphConfigScreen;
import net.blockomorph.utils.BlockInPlayer;
import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.accessors.LevelRendererAccessor;
import net.blockomorph.utils.MorphUtils;
import net.blockomorph.utils.PlayerAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
    private final EntityRenderDispatcher entityDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
    private final ItemInHandRenderer itemRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
    private final BlockPos AIR = new BlockPos(0, 512, 0);
    private final RandomSource random = RandomSource.create();

    private final Minecraft mc = Minecraft.getInstance();

    public PlayerRendererMixin(EntityRendererProvider.Context p_174557_, boolean p_174558_) {
        super(p_174557_, new PlayerModel<>(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), p_174558_), 0.5F);
    }

    @Inject(
            method = {"render"},
            at = {@At("HEAD")},
            cancellable = true
    )
    public void render(AbstractClientPlayer player, float anim, float partialticks, PoseStack posestack, MultiBufferSource buffer, int light, CallbackInfo info) {
        if (player instanceof PlayerAccessor pl) {
            if (pl.isFullActive()) {
                info.cancel();
                posestack.pushPose();
                this.adjustMatrixForPlayer(posestack, pl, player);
                this.renderBlock(player, posestack, buffer, pl);
                this.renderBlockEntity(player, partialticks, posestack, buffer, light, pl);
                this.renderBreak(player, posestack, buffer, pl);
                this.renderFrame(player, posestack, buffer, pl);
                posestack.popPose();
                this.shadowRadius = 0.0f;
            } else {
                if (pl.getTnt() != null) {
                    info.cancel();
                    this.renderTnt(player, anim, partialticks, posestack, buffer, light, pl);
                }
                this.shadowRadius = 0.5f;
            }
        }
    }

    private void renderTnt(AbstractClientPlayer player, float anim, float partialticks, PoseStack posestack, MultiBufferSource buffer, int light, PlayerAccessor pl) {
        PrimedTnt tnt = pl.getTnt();
        EntityRenderer rend = entityDispatcher.getRenderer(pl.getTnt());
        try {
            rend.render(tnt, anim, partialticks, posestack, buffer, light);
        } catch (Exception e) {
            if (player == Minecraft.getInstance().player && Minecraft.getInstance().screen instanceof BlockMorphConfigScreen sc)
                sc.tagException = e.getMessage();
        }
    }

    private void adjustMatrixForPlayer(PoseStack poseStack, PlayerAccessor pl, AbstractClientPlayer player) {
        BlockPos minpos = pl.minPos();
        AABB hitbox = player.getBoundingBox();
        Vec3 playerCenter = player.position();

        double offsetX = hitbox.minX - (playerCenter.x + minpos.getX());
        double offsetZ = hitbox.minZ - (playerCenter.z + minpos.getZ());

        poseStack.translate(offsetX, 0.0, offsetZ);
    }

    private ModelData getData(PlayerAccessor pl, BlockPos offset) {
        UseController ctr = pl.getUseControllers().get(offset);
        if (ctr != null) return ctr.getData();
        return ModelData.EMPTY;
    }

    private BlockPos getPosForOffset(UseController ctr) {
        return BlockPosAccessor.of(BlockPos.containing(ctr.getRealPos())).setUseController(ctr);
    }

    private void renderBlock(AbstractClientPlayer player, PoseStack posestack, MultiBufferSource buffer, PlayerAccessor pl) {
        for (Map.Entry<BlockPos, BlockInPlayer> entry : pl.getBlocksData().entrySet()) {
            BlockInPlayer bl = entry.getValue();
            BlockState st = bl.getBlockState();
            BlockPos pos = entry.getKey();
            posestack.pushPose();
            posestack.translate(pos.getX(), pos.getY(), pos.getZ());
            if (st.getBlock() instanceof LiquidBlock) {
                //this.renderLiquid(pos, st, posestack, buffer, pl); //TODO
            } else if (st.getRenderShape() == RenderShape.MODEL) {
                this.renderBlock(player, st, posestack, buffer, this.getPosForOffset(bl.getUseController()), this.getData(pl, pos));
            }
            posestack.popPose();
        }
    }

    private void renderBlock(AbstractClientPlayer player, BlockState blockstate, PoseStack posestack, MultiBufferSource buffer, BlockPos offset, ModelData data) {
        Level level = player.level();
        var model = this.dispatcher.getBlockModel(blockstate);
        for (var renderType : model.getRenderTypes(blockstate, this.random, ModelData.EMPTY))
            this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, offset, posestack, buffer.getBuffer(renderType), false, this.random, blockstate.getSeed(offset), OverlayTexture.NO_OVERLAY, data, renderType);
    }

    private void renderLiquid(BlockPos offset, BlockState blockstate, PoseStack posestack, MultiBufferSource buffer, PlayerAccessor pl) {
        //this.dispatcher.renderLiquid(BlockPos.ZERO, pl.getLiquidCachedLevel(), buffer.getBuffer(ItemBlockRenderTypes.getRenderLayer(blockstate.getFluidState())), blockstate, blockstate.getFluidState());
        //PlayerLiquidRenderer.render(pl.player().level(), posestack, buffer, LightTexture.pack(15, 15), blockstate.getFluidState());
    }

    private void renderBlockEntity(AbstractClientPlayer player, float partialticks, PoseStack posestack, MultiBufferSource buffer, int light, PlayerAccessor pl) {
        //this.renderBlockEntity(BlockPos.ZERO, player, partialticks, posestack, buffer, light, pl);
        for (Map.Entry<BlockPos, BlockState> entry : pl.getBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            posestack.pushPose();
            posestack.translate(pos.getX(), pos.getY(), pos.getZ());
            BlockPos offset = player.blockPosition().offset(pos);
            this.renderBlockEntity(entry.getKey(), player, partialticks, posestack, buffer, this.getRenderLight(player, offset), pl);
            posestack.popPose();
        }
    }

    private void renderBlockEntity(BlockPos offset, AbstractClientPlayer player, float partialticks, PoseStack posestack, MultiBufferSource buffer, int light, PlayerAccessor pl) {
        BlockEntity blockEntity = pl.getUseControllers().get(offset).getBlockEntity();
        if (blockEntity != null) {
            posestack.pushPose();
            try {
                BlockEntityRenderer renderer = blockEntityRenderDispatcher.getRenderer(blockEntity);
                if (renderer != null) {
                    CompoundTag tg = pl.getProgress();
                    MultiBufferSource src = buffer;
                    if (tg.contains(MorphUtils.getBlockPos(offset))) {
                        int k = tg.getInt(MorphUtils.getBlockPos(offset));
                        if (k > -1 && k < 10) {
                            PoseStack.Pose posestack$pose = posestack.last();
                            VertexConsumer vertexconsumer = new SheetedDecalTextureGenerator(buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(k)), posestack$pose.pose(), posestack$pose.normal(), 1.0F);
                            src = (p_234298_) -> {
                                VertexConsumer vertexconsumer2 = buffer.getBuffer(p_234298_);
                                return p_234298_.affectsCrumbling() ? VertexMultiConsumer.create(vertexconsumer, vertexconsumer2) : vertexconsumer2;
                            };
                        }
                    }
                    renderer.render(blockEntity, partialticks, posestack, src, light, OverlayTexture.NO_OVERLAY);
                }
            } catch (Exception e) {
                if (player == Minecraft.getInstance().player && Minecraft.getInstance().screen instanceof BlockMorphConfigScreen sc)
                    sc.tagException = e.getMessage() == null ? e.getClass().toString() : e.getMessage();
            } finally {
                posestack.popPose();
            }
        }
    }

    protected int getRenderLight(AbstractClientPlayer entity, BlockPos blockPos) {
        return LightTexture.pack(this.getBlockLightLevel(entity, blockPos), this.getSkyLightLevel(entity, blockPos));
    }

    private void renderBreak(AbstractClientPlayer player, PoseStack posestack, MultiBufferSource buffer, PlayerAccessor pl) {
        CompoundTag k = pl.getProgress();
        for (String key : k.getAllKeys()) {
            if (key.equals("fuse")) continue;
            posestack.pushPose();

            BlockPos pos = MorphUtils.parseBlockPos(key);
            BlockState state = pl.getBlocks().get(pos);
            if (state != null && pos != null && state.getRenderShape() == RenderShape.MODEL) {
                posestack.translate(pos.getX(), pos.getY(), pos.getZ());
                this.renderBreak(k.getInt(key), state, player, posestack, buffer, this.getData(pl, pos), pl, pos);
            }

            posestack.popPose();
        }
    }

    private void renderBreak(int k, BlockState blockstate, AbstractClientPlayer player, PoseStack posestack, MultiBufferSource buffer, ModelData data, PlayerAccessor pl, BlockPos offset) {
        posestack.pushPose();
        UseController ctr = pl.getUseControllers().get(offset);
        PoseStack.Pose posestack$pose1 = posestack.last();
        if (k > -1 && k < 10) {
            VertexConsumer vertexconsumer1 = new SheetedDecalTextureGenerator(buffer.getBuffer(ModelBakery.DESTROY_TYPES.get(k)), posestack$pose1.pose(), posestack$pose1.normal(), 1.0F);
            this.dispatcher.getModelRenderer().tesselateBlock(player.level(), this.dispatcher.getBlockModel(blockstate), blockstate, this.getPosForOffset(ctr), posestack, vertexconsumer1, false, this.random, blockstate.getSeed(this.getPosForOffset(ctr)), OverlayTexture.NO_OVERLAY, data, null);
        }

        posestack.popPose();
    }

    private void renderFrame(AbstractClientPlayer player, PoseStack posestack, MultiBufferSource buffer, PlayerAccessor pl) {
        Minecraft mc = Minecraft.getInstance();
        if (this.shouldRenderFrame(mc, player, pl)) {
            posestack.pushPose();
            VoxelShape shape = pl.getRenderShape(MorphUtils.hitPart);
            if (shape == null) {
                posestack.popPose();
                return;
            }
            ((LevelRendererAccessor) mc.levelRenderer).renderBlockHitbox(posestack, buffer.getBuffer(RenderType.lines()), shape, 0, 0, 0, 0f, 0f, 0f, 0.4f);
            posestack.popPose();
        }
    }

    private boolean shouldRenderFrame(Minecraft mc, AbstractClientPlayer player, PlayerAccessor pl) {
        if (player == MorphUtils.hitEntity && MorphUtils.hitPart != null && !mc.options.hideGui) {
            if (mc.player.isSpectator()) {
                return MorphUtils.canOpenMenuIn(pl, MorphUtils.hitPart);
            } else if (mc.gameMode.getPlayerMode() == GameType.ADVENTURE) {
                return MorphUtils.isAdventureCanBreak(pl, mc.player, MorphUtils.hitPart);
            }
            return true;
        }
        return false;
    }

}
