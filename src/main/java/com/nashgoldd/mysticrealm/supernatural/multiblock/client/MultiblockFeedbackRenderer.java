package com.nashgoldd.mysticrealm.supernatural.multiblock.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Desenha o feedback visual de validação de estruturas multiblock (outlines verdes/vermelhos).
 * Genérico: lê {@link ClientStructureFeedback}, que pode ser preenchido por qualquer
 * {@link com.nashgoldd.mysticrealm.supernatural.multiblock.IMultiblockStructure}.
 */
@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public final class MultiblockFeedbackRenderer {

    private static final VoxelShape UNIT_CUBE = Shapes.create(new AABB(0, 0, 0, 1, 1, 1));

    private static final int COLOR_MATCHED = 0x8000FF00; // verde
    private static final int COLOR_MISSING = 0x80FF0000; // vermelho
    private static final int COLOR_WRONG   = 0x80FF8000; // laranja

    private MultiblockFeedbackRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        if (!ClientStructureFeedback.isActive()) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.lines());

        for (BlockPos pos : ClientStructureFeedback.matchedBlocks) {
            renderOutline(poseStack, consumer, pos, camera, COLOR_MATCHED);
        }
        for (BlockPos pos : ClientStructureFeedback.missingBlocks) {
            renderOutline(poseStack, consumer, pos, camera, COLOR_MISSING);
        }
        for (BlockPos pos : ClientStructureFeedback.wrongBlocks) {
            renderOutline(poseStack, consumer, pos, camera, COLOR_WRONG);
        }
    }

    private static void renderOutline(PoseStack poseStack, VertexConsumer consumer, BlockPos pos, Vec3 camera, int argbColor) {
        Vec3 offset = Vec3.atLowerCornerOf(pos).subtract(camera);

        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        ShapeRenderer.renderShape(poseStack, consumer, UNIT_CUBE, 0, 0, 0, argbColor, 1F);
        poseStack.popPose();
    }
}
