package com.nashgoldd.mysticrealm.supernatural.multiblock.effect;

import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

/**
 * Partículas de feedback (one-shot) para qualquer {@link MultiblockValidationResult} —
 * genérico, não depende da estrutura validada.
 */
public final class MultiblockFeedbackEffects {

    private static final DustParticleOptions WRONG_DUST = new DustParticleOptions(0xFF3333, 1.0f);

    private MultiblockFeedbackEffects() {}

    public static void spawnFeedbackParticles(ServerLevel level, MultiblockValidationResult result) {
        for (BlockPos pos : result.matchedBlocks()) {
            spawn(level, ParticleTypes.HAPPY_VILLAGER, pos);
        }
        for (BlockPos pos : result.invalidPositions()) {
            spawn(level, WRONG_DUST, pos);
        }
    }

    private static void spawn(ServerLevel level, net.minecraft.core.particles.ParticleOptions options, BlockPos pos) {
        level.sendParticles(options, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.0);
    }
}
