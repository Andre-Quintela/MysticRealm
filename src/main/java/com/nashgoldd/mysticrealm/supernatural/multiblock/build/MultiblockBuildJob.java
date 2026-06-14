package com.nashgoldd.mysticrealm.supernatural.multiblock.build;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.supernatural.multiblock.IMultiblockController;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockPattern;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidationResult;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidator;
import com.nashgoldd.mysticrealm.supernatural.multiblock.effect.MultiblockFeedbackEffects;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.SyncStructureValidationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Construção sequencial e adiada de uma {@link MultiblockPattern}: coloca um bloco a cada
 * {@link MysticConfig#MULTIBLOCK_BUILD_DELAY_TICKS} ticks, tocando o som de colocação de cada
 * bloco, para dar a sensação de construção mágica. Genérico — sem referência a vampiros.
 */
public final class MultiblockBuildJob {

    private final ServerLevel level;
    private final BlockPos center;
    private final MultiblockPattern pattern;
    private final UUID playerId;
    private final Deque<PendingPlacement> placements;
    private int ticksUntilNext;

    public MultiblockBuildJob(ServerLevel level, BlockPos center, MultiblockPattern pattern,
                               UUID playerId, List<PendingPlacement> placements) {
        this.level = level;
        this.center = center;
        this.pattern = pattern;
        this.playerId = playerId;
        this.placements = new ArrayDeque<>(placements);
        this.ticksUntilNext = MysticConfig.MULTIBLOCK_BUILD_DELAY_TICKS.get();
    }

    /**
     * Avança a fila em um tick. @return true se o job terminou e deve ser removido.
     */
    public boolean tick() {
        if (placements.isEmpty()) return true;
        if (!level.isLoaded(center)) return true;

        if (--ticksUntilNext > 0) return false;

        PendingPlacement next = placements.poll();
        BlockState state = next.block().defaultBlockState();
        level.setBlockAndUpdate(next.pos(), state);
        level.playSound(null, next.pos(), state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);

        if (placements.isEmpty()) {
            onComplete();
            return true;
        }

        ticksUntilNext = MysticConfig.MULTIBLOCK_BUILD_DELAY_TICKS.get();
        return false;
    }

    private void onComplete() {
        MultiblockValidationResult result = MultiblockValidator.validate(level, center, pattern);

        if (level.getBlockEntity(center) instanceof IMultiblockController controller) {
            controller.setCachedResult(result);
        }

        MultiblockFeedbackEffects.spawnFeedbackParticles(level, result);

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player != null) {
            PacketDistributor.sendToPlayer(player, SyncStructureValidationPacket.from(center, result));
        }
    }
}
