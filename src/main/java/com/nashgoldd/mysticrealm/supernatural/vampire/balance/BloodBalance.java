package com.nashgoldd.mysticrealm.supernatural.vampire.balance;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import net.minecraft.world.entity.LivingEntity;

public final class BloodBalance {

    private BloodBalance() {}

    public static float maxBloodForEntity(LivingEntity entity) {
        return Math.max(1.0f, entity.getMaxHealth() / 2.0f);
    }

    public static float drainAmountPerInterval() {
        return MysticConfig.BLOOD_DRAIN_AMOUNT_PER_INTERVAL.get().floatValue();
    }

    public static float exsanguinationDamage() {
        return MysticConfig.ENTITY_EXSANGUINATION_DAMAGE.get().floatValue();
    }

    public static float bloodRegenRate() {
        return MysticConfig.ENTITY_BLOOD_REGEN_RATE.get().floatValue();
    }

    public static int bloodRegenIntervalTicks() {
        return MysticConfig.ENTITY_BLOOD_REGEN_INTERVAL_TICKS.get();
    }

    // Total de +4 food por drenagem completa (60 ticks / 5 ticks por intervalo = 12 intervalos)
    public static float foodPerInterval() {
        return 4.0f / 12.0f;
    }
}
