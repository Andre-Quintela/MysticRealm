package com.nashgoldd.mysticrealm.supernatural.multiblock.client;

import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockBuildResult;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.SyncStructureBuildResultPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Estado client-side com o resultado mais recente de uma tentativa de construção automática
 * de estrutura multiblock, usado pela tela do controlador para exibir feedback. Expira sozinho
 * após {@link #DISPLAY_DURATION_MS} para não deixar mensagens "presas" na tela.
 */
public final class ClientBuildFeedback {

    private static final long DISPLAY_DURATION_MS = 6000L;

    public static volatile BlockPos controllerPos;
    public static volatile boolean success;
    public static volatile MultiblockBuildResult.Reason reason;
    public static volatile List<ItemStack> missingItems = List.of();
    public static volatile float healthCost;
    private static volatile long receivedAtMillis;

    private ClientBuildFeedback() {}

    public static void update(SyncStructureBuildResultPacket packet) {
        controllerPos = packet.controllerPos();
        success = packet.success();
        reason = MultiblockBuildResult.Reason.values()[packet.reasonOrdinal()];
        missingItems = packet.missingItems();
        healthCost = packet.healthCost();
        receivedAtMillis = System.currentTimeMillis();
    }

    public static void clear() {
        controllerPos = null;
        missingItems = List.of();
    }

    public static boolean isActive() {
        return controllerPos != null && (System.currentTimeMillis() - receivedAtMillis) < DISPLAY_DURATION_MS;
    }
}
