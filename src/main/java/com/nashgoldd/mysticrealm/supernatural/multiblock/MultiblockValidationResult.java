package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * Resultado da validação de um {@link MultiblockPattern} contra o mundo, usado tanto para
 * decidir se um ritual pode começar quanto para alimentar feedback visual ao jogador.
 */
public record MultiblockValidationResult(
    boolean valid,
    List<BlockPos> missingBlocks,
    List<BlockPos> wrongBlocks,
    List<BlockPos> invalidPositions,
    List<BlockPos> matchedBlocks,
    float percentCompleted
) {
    public static final MultiblockValidationResult EMPTY =
        new MultiblockValidationResult(false, List.of(), List.of(), List.of(), List.of(), 0f);
}
