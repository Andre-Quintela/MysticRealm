package com.nashgoldd.mysticrealm.supernatural.vampire.essence;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public final class BloodEssenceRegistry {

    private static final long DEFAULT_ESSENCE = 1L;

    private static final Map<EntityType<?>, Long> ESSENCE_VALUES = Map.ofEntries(
        Map.entry(EntityType.CHICKEN, 1L),
        Map.entry(EntityType.COD, 1L),
        Map.entry(EntityType.SALMON, 1L),
        Map.entry(EntityType.RABBIT, 1L),
        Map.entry(EntityType.PIG, 2L),
        Map.entry(EntityType.SHEEP, 2L),
        Map.entry(EntityType.GOAT, 2L),
        Map.entry(EntityType.COW, 3L),
        Map.entry(EntityType.MOOSHROOM, 3L),
        Map.entry(EntityType.DONKEY, 3L),
        Map.entry(EntityType.MULE, 3L),
        Map.entry(EntityType.LLAMA, 3L),
        Map.entry(EntityType.HORSE, 4L),
        Map.entry(EntityType.CAMEL, 4L),
        Map.entry(EntityType.VILLAGER, 10L),
        Map.entry(EntityType.WANDERING_TRADER, 12L),
        Map.entry(EntityType.PILLAGER, 8L),
        Map.entry(EntityType.WITCH, 9L),
        Map.entry(EntityType.VINDICATOR, 8L),
        Map.entry(EntityType.EVOKER, 12L),
        Map.entry(EntityType.ILLUSIONER, 11L)
    );

    private BloodEssenceRegistry() {}

    public static long getBaseEssence(LivingEntity entity) {
        if (entity instanceof Player) return 15L;
        return ESSENCE_VALUES.getOrDefault(entity.getType(), DEFAULT_ESSENCE);
    }

    /**
     * Retorna essência proporcional ao sangue drenado desta entidade neste intervalo.
     * fractionDrained = actuallyDrained / maxBlood (0.0–1.0).
     */
    public static long getProportionalEssence(LivingEntity entity, float fractionDrained) {
        long base = getBaseEssence(entity);
        return Math.max(0L, Math.round(base * fractionDrained));
    }
}
