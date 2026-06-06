package com.nashgoldd.mysticrealm.supernatural.vampire.feeding;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.EntityBloodData;
import com.nashgoldd.mysticrealm.supernatural.vampire.balance.BloodBalance;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCancelEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCompleteEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainInterruptedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.essence.BloodEssenceRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainStartEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainTickEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodPoolChangedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.EntityExsanguinatedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireProgressionService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BloodDrainAction implements ChannelAction {

    public static final BloodDrainAction INSTANCE = new BloodDrainAction();
    public static final String ID = "blood_drain";

    // Acumula food fracionário por vampiro para aplicar de forma contínua
    private static final Map<UUID, Float> bloodAccumulator = new HashMap<>();

    // Acumula essência fracionária por vampiro — necessário pois base*fraction é < 1 para animais pequenos
    private static final Map<UUID, Double> essenceAccumulator = new HashMap<>();

    private BloodDrainAction() {}

    @Override public String getActionId()   { return ID; }
    @Override public int getDurationTicks() { return 60; }
    @Override public int getCooldownTicks() { return 100; }

    @Override
    public boolean isValidTarget(LivingEntity target, ServerPlayer actor) {
        return DrainableEntityRegistry.isValidTarget(target, actor);
    }

    @Override
    public void onStart(LivingEntity target, ServerPlayer actor) {
        NeoForge.EVENT_BUS.post(new BloodDrainStartEvent(actor, target));
        MysticNetwork.syncDrainToClient(actor);
    }

    @Override
    public void onTick(LivingEntity target, ServerPlayer actor, int ticksElapsed) {
        if (ticksElapsed % 5 != 0) return;

        ServerLevel sl = (ServerLevel) actor.level();
        Vec3 neck = target.getEyePosition().subtract(0, 0.3, 0);

        sl.sendParticles(ParticleTypes.HEART,
            neck.x, neck.y, neck.z, 3, 0.15, 0.1, 0.15, 0.0);
        sl.playSound(null, actor.blockPosition(), SoundEvents.WITCH_DRINK,
            SoundSource.PLAYERS, 0.5f, 0.5f);

        // ── Pool de sangue da entidade ────────────────────────────────────────
        EntityBloodData bloodData = EntityBloodData.getOrInit(target);
        float oldBlood = bloodData.getCurrentBlood();
        boolean wasEmpty = bloodData.isEmpty();

        if (bloodData.isEmpty()) {
            // Exsanguinação: pool esgotado — aplica dano periódico
            float dmg = BloodBalance.exsanguinationDamage();
            target.hurtServer(sl, sl.damageSources().playerAttack(actor), dmg);
        } else {
            // Drenar do pool da entidade
            float actuallyDrained = bloodData.drain(BloodBalance.drainAmountPerInterval());
            target.setData(MysticAttachments.ENTITY_BLOOD, bloodData);

            // Essência proporcional ao sangue drenado — acumulada para evitar arredondamento para zero
            float maxBlood = bloodData.getMaxBlood();
            if (maxBlood > 0 && actuallyDrained > 0) {
                float fraction = actuallyDrained / maxBlood;
                double essenceFrac = BloodEssenceRegistry.getBaseEssence(target) * fraction;
                UUID uid = actor.getUUID();
                double accumulated = essenceAccumulator.getOrDefault(uid, 0.0) + essenceFrac;
                long toGrant = (long) accumulated;
                essenceAccumulator.put(uid, accumulated - toGrant);
                if (toGrant > 0) {
                    VampireProgressionService.grantEssence(actor, toGrant, target);
                }
            }

            float newBlood = bloodData.getCurrentBlood();

            NeoForge.EVENT_BUS.post(new BloodPoolChangedEvent(
                target, oldBlood, newBlood, bloodData.getMaxBlood(),
                BloodPoolChangedEvent.ChangeReason.DRAINED));

            if (!wasEmpty && bloodData.isEmpty()) {
                NeoForge.EVENT_BUS.post(new EntityExsanguinatedEvent(target, actor));
            }
        }

        // ── Acumulador fracionário de food para o vampiro ─────────────────────
        UUID uid = actor.getUUID();
        float accumulated = bloodAccumulator.getOrDefault(uid, 0f) + BloodBalance.foodPerInterval();
        int toApply = (int) accumulated;
        bloodAccumulator.put(uid, accumulated - toApply);

        int foodApplied = 0;
        if (toApply > 0) {
            FoodData food = actor.getFoodData();
            food.setFoodLevel(Math.min(20, food.getFoodLevel() + toApply));
            foodApplied = toApply;
            MysticNetwork.syncVampireToClient(actor);
        }

        NeoForge.EVENT_BUS.post(new BloodDrainTickEvent(
            actor, target,
            bloodData.isEmpty() ? 0f : BloodBalance.drainAmountPerInterval(),
            foodApplied, bloodData.isEmpty()));
    }

    @Override
    public void onComplete(LivingEntity target, ServerPlayer actor) {
        // Descarta fração remanescente — os intervalos anteriores já cobriram o total
        bloodAccumulator.remove(actor.getUUID());
        essenceAccumulator.remove(actor.getUUID());

        // Penalidade de Fraqueza na vítima (mantida do sistema original)
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true));

        // Aldeões: penalidade adicional
        if (target.getType() == EntityType.VILLAGER || target.getType() == EntityType.WANDERING_TRADER) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0, false, true));
            ServerLevel sl = (ServerLevel) actor.level();
            sl.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_HURT,
                SoundSource.NEUTRAL, 1.0f, 1.0f);
        }

        NeoForge.EVENT_BUS.post(new BloodDrainCompleteEvent(actor, target));
        MysticNetwork.syncVampireToClient(actor);
        MysticNetwork.syncDrainToClient(actor);
    }

    @Override
    public void onInterrupt(LivingEntity target, ServerPlayer actor, String reason) {
        // Progresso já foi aplicado tick a tick — não há perda ao interromper
        bloodAccumulator.remove(actor.getUUID());
        essenceAccumulator.remove(actor.getUUID());

        if ("cancel".equals(reason)) {
            NeoForge.EVENT_BUS.post(new BloodDrainCancelEvent(actor, target));
        } else {
            NeoForge.EVENT_BUS.post(new BloodDrainInterruptedEvent(actor, target, reason));
        }
        MysticNetwork.syncDrainToClient(actor);
    }

    public static void clearAccumulator(UUID playerId) {
        bloodAccumulator.remove(playerId);
        essenceAccumulator.remove(playerId);
    }
}
