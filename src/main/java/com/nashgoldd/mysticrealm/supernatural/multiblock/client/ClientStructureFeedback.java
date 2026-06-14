package com.nashgoldd.mysticrealm.supernatural.multiblock.client;

import com.nashgoldd.mysticrealm.supernatural.multiblock.network.SyncStructureValidationPacket;
import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Estado client-side com o resultado mais recente de uma validação de estrutura multiblock,
 * usado por {@link MultiblockFeedbackRenderer} para desenhar outlines. Expira sozinho após
 * {@link #DISPLAY_DURATION_MS} para não deixar highlights "presos" na tela.
 */
public final class ClientStructureFeedback {

    private static final long DISPLAY_DURATION_MS = 6000L;

    public static volatile BlockPos controllerPos;
    public static volatile boolean valid;
    public static volatile List<BlockPos> matchedBlocks = List.of();
    public static volatile List<BlockPos> missingBlocks = List.of();
    public static volatile List<BlockPos> wrongBlocks = List.of();
    public static volatile float percentCompleted;
    private static volatile long receivedAtMillis;

    private ClientStructureFeedback() {}

    public static void update(SyncStructureValidationPacket packet) {
        controllerPos = packet.controllerPos();
        valid = packet.valid();
        matchedBlocks = packet.matchedBlocks();
        missingBlocks = packet.missingBlocks();
        wrongBlocks = packet.wrongBlocks();
        percentCompleted = packet.percentCompleted();
        receivedAtMillis = System.currentTimeMillis();
    }

    public static void clear() {
        controllerPos = null;
        matchedBlocks = List.of();
        missingBlocks = List.of();
        wrongBlocks = List.of();
    }

    public static boolean isActive() {
        return controllerPos != null && (System.currentTimeMillis() - receivedAtMillis) < DISPLAY_DURATION_MS;
    }
}
