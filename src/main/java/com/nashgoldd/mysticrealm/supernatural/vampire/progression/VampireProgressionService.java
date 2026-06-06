package com.nashgoldd.mysticrealm.supernatural.vampire.progression;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodEssenceGainedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireAgeMilestoneEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireAscensionEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireRankChangedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

public final class VampireProgressionService {

    // Milestones em horas que disparam VampireAgeMilestoneEvent
    private static final long[] MILESTONE_HOURS = { 1L, 5L, 15L, 50L, 100L, 250L };

    private static final long TICKS_PER_HOUR = 72000L;

    private VampireProgressionService() {}

    /**
     * Verifica se o jogador satisfaz os requisitos para ascender ao próximo rank.
     */
    public static boolean canAscend(ServerPlayer player) {
        if (!MysticConfig.ENABLE_VAMPIRE_PROGRESSION.get()) return false;
        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        VampireRank current = data.getRank();
        if (current.isMax()) return false;

        long requiredEssence = getRequiredEssence(current);
        long requiredAgeTicks = getRequiredAgeHours(current) * TICKS_PER_HOUR;

        return data.getBloodEssence() >= requiredEssence
            && data.getVampireAgeTicks() >= requiredAgeTicks;
    }

    /**
     * Executa a ascensão para o próximo rank. Não valida requisitos — use canAscend() antes.
     */
    public static void ascend(ServerPlayer player) {
        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        VampireRank previous = data.getRank();
        VampireRank next = previous.next().orElseThrow();

        data.setRank(next);
        data.incrementAscensionCount();

        NeoForge.EVENT_BUS.post(new VampireRankChangedEvent(player, previous, next));
        NeoForge.EVENT_BUS.post(new VampireAscensionEvent(player, next, data.getAscensionCount()));

        MysticNetwork.syncVampireProgressionToClient(player);
    }

    /**
     * Concede essência permanente ao vampiro a partir de uma fonte de drenagem.
     * source pode ser null se a essência vier de outra origem.
     */
    public static void grantEssence(ServerPlayer player, long amount, LivingEntity source) {
        if (!MysticConfig.ENABLE_VAMPIRE_PROGRESSION.get() || amount <= 0) return;

        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        data.addBloodEssence(amount);

        NeoForge.EVENT_BUS.post(new BloodEssenceGainedEvent(player, amount, data.getBloodEssence(), source));
        MysticNetwork.syncVampireProgressionToClient(player);
    }

    /**
     * Incrementa a idade vampírica em 1 tick. Dispara eventos de milestone ao cruzar limites.
     * Deve ser chamado a cada tick de servidor enquanto o jogador for vampiro.
     */
    public static void tickAge(ServerPlayer player) {
        if (!MysticConfig.TRACK_VAMPIRE_AGE.get()) return;

        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        long previousTicks = data.getVampireAgeTicks();
        data.incrementVampireAgeTicks();
        long currentTicks = data.getVampireAgeTicks();

        for (long milestoneHours : MILESTONE_HOURS) {
            long milestoneTicks = milestoneHours * TICKS_PER_HOUR;
            if (previousTicks < milestoneTicks && currentTicks >= milestoneTicks) {
                NeoForge.EVENT_BUS.post(new VampireAgeMilestoneEvent(player, currentTicks, milestoneHours));
                break;
            }
        }
    }

    // ── Helpers de requisitos ─────────────────────────────────────────────────

    public static long getRequiredEssence(VampireRank rank) {
        return switch (rank) {
            case NEWBORN        -> MysticConfig.NEWBORN_TO_NEOPHYTE_ESSENCE.get();
            case NEOPHYTE       -> MysticConfig.NEOPHYTE_TO_VAMPIRE_ESSENCE.get();
            case VAMPIRE        -> MysticConfig.VAMPIRE_TO_ELDER_ESSENCE.get();
            case ELDER          -> MysticConfig.ELDER_TO_LORD_ESSENCE.get();
            case VAMPIRE_LORD   -> MysticConfig.LORD_TO_PRINCE_ESSENCE.get();
            case PRINCE_OF_NIGHT -> MysticConfig.PRINCE_TO_SOVEREIGN_ESSENCE.get();
            case BLOOD_SOVEREIGN -> Long.MAX_VALUE;
        };
    }

    public static long getRequiredAgeHours(VampireRank rank) {
        return switch (rank) {
            case NEWBORN        -> MysticConfig.NEWBORN_TO_NEOPHYTE_AGE_HOURS.get();
            case NEOPHYTE       -> MysticConfig.NEOPHYTE_TO_VAMPIRE_AGE_HOURS.get();
            case VAMPIRE        -> MysticConfig.VAMPIRE_TO_ELDER_AGE_HOURS.get();
            case ELDER          -> MysticConfig.ELDER_TO_LORD_AGE_HOURS.get();
            case VAMPIRE_LORD   -> MysticConfig.LORD_TO_PRINCE_AGE_HOURS.get();
            case PRINCE_OF_NIGHT -> MysticConfig.PRINCE_TO_SOVEREIGN_AGE_HOURS.get();
            case BLOOD_SOVEREIGN -> Long.MAX_VALUE;
        };
    }
}
