package com.nashgoldd.mysticrealm.supernatural.vampire.balance;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import net.minecraft.world.entity.LivingEntity;

public final class BloodBalance {

    private BloodBalance() {}

    public static float maxBloodForEntity(LivingEntity entity) {
        return Math.max(1.0f, entity.getMaxHealth());
    }

    public static float drainAmountPerInterval() {
        return MysticConfig.BLOOD_DRAIN_AMOUNT_PER_INTERVAL.get().floatValue();
    }

    public static float exsanguinationDamage() {
        return MysticConfig.ENTITY_EXSANGUINATION_DAMAGE.get().floatValue();
    }

    // Fração do maxBlood a regenerar por intervalo (ex: 0.05 = 5%)
    public static float bloodRegenFraction() {
        return MysticConfig.ENTITY_BLOOD_REGEN_FRACTION.get().floatValue();
    }

    public static int bloodRegenIntervalTicks() {
        return MysticConfig.ENTITY_BLOOD_REGEN_INTERVAL_TICKS.get();
    }

    // Total de +4 food por drenagem completa (60 ticks / 5 ticks por intervalo = 12 intervalos)
    public static float foodPerInterval() {
        return 4.0f / 12.0f;
    }
}
