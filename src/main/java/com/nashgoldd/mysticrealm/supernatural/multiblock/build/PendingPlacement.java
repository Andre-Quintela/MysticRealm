package com.nashgoldd.mysticrealm.supernatural.multiblock.build;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

/**
 * Um único bloco a ser colocado por um {@link MultiblockBuildJob}, na ordem da fila.
 */
public record PendingPlacement(BlockPos pos, Block block) {
}
