package com.nashgoldd.mysticrealm.supernatural.vampire.feeding;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.EntityBloodData;
import com.nashgoldd.mysticrealm.supernatural.vampire.balance.BloodBalance;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCancelEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCompleteEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainInterruptedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainStartEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainTickEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodPoolChangedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.EntityExsanguinatedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireProgressionService;
import com.nashgoldd.mysticrealm.registry.MysticParticles;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BloodDrainAction implements ChannelAction {

    public static final BloodDrainAction INSTANCE = new BloodDrainAction();
    public static final String ID = "blood_drain";

    // Acumula sangue fracionário drenado por vampiro — convertido 1:1 em food units e essência
    // quando atinge >= 1 (necessário pois drainAmountPerInterval costuma ser < 1)
    private static final Map<UUID, Double> drainAccumulator = new HashMap<>();

    // Rastreia se sangue foi drenado nesta sessão — safe stop só dispara se o pool
    // esvaziou DURANTE a ação, não se já estava vazio ao iniciar
    private static final Set<UUID> drainedThisSession = new HashSet<>();

    private BloodDrainAction() {}

    @Override public String getActionId()   { return ID; }
    @Override public int getDurationTicks() { return Integer.MAX_VALUE; }
    @Override public int getCooldownTicks() { return 100; }

    @Override
    public boolean shouldSafeStop(LivingEntity target, ServerPlayer actor, int ticksElapsed) {
        return drainedThisSession.contains(actor.getUUID())
            && EntityBloodData.getOrInit(target).isEmpty();
    }

    @Override
    public boolean isValidTarget(LivingEntity target, ServerPlayer actor) {
        return DrainableEntityRegistry.isValidTarget(target, actor);
    }

    @Override
    public void onStart(LivingEntity target, ServerPlayer actor) {
        drainedThisSession.remove(actor.getUUID());
        NeoForge.EVENT_BUS.post(new BloodDrainStartEvent(actor, target));
        MysticNetwork.syncDrainToClient(actor);
    }

    @Override
    public void onTick(LivingEntity target, ServerPlayer actor, int ticksElapsed) {
        if (ticksElapsed % 5 != 0) return;

        ServerLevel sl = (ServerLevel) actor.level();
        Vec3 neck = target.getEyePosition().subtract(0, 0.1, 0);

        sl.sendParticles(MysticParticles.BLOOD_DRAIN.get(),
            neck.x, neck.y, neck.z, 3, 0.15, 0.1, 0.15, 0.0);
        sl.playSound(null, actor.blockPosition(), SoundEvents.WITCH_DRINK,
            SoundSource.PLAYERS, 0.5f, 0.5f);

        // ── Pool de sangue da entidade ────────────────────────────────────────
        EntityBloodData bloodData = EntityBloodData.getOrInit(target);
        float oldBlood = bloodData.getCurrentBlood();
        boolean wasEmpty = bloodData.isEmpty();
        float actuallyDrained = 0f;

        if (bloodData.isEmpty()) {
            // Exsanguinação: pool esgotado — aplica dano periódico
            float dmg = BloodBalance.exsanguinationDamage();
            target.hurtServer(sl, sl.damageSources().playerAttack(actor), dmg);
        } else {
            // Drenar do pool da entidade
            actuallyDrained = bloodData.drain(BloodBalance.drainAmountPerInterval());
            target.setData(MysticAttachments.ENTITY_BLOOD, bloodData);
            drainedThisSession.add(actor.getUUID());

            float newBlood = bloodData.getCurrentBlood();

            NeoForge.EVENT_BUS.post(new BloodPoolChangedEvent(
                target, oldBlood, newBlood, bloodData.getMaxBlood(),
                BloodPoolChangedEvent.ChangeReason.DRAINED));

            if (!wasEmpty && bloodData.isEmpty()) {
                NeoForge.EVENT_BUS.post(new EntityExsanguinatedEvent(target, actor));
            }
        }

        // ── Acumulador fracionário de sangue drenado — converte 1:1 em food + essência ──
        UUID uid = actor.getUUID();
        int foodApplied = 0;
        if (actuallyDrained > 0) {
            double accumulated = drainAccumulator.getOrDefault(uid, 0.0) + actuallyDrained;
            long toGrant = (long) accumulated;
            drainAccumulator.put(uid, accumulated - toGrant);

            if (toGrant > 0) {
                VampireProgressionService.grantEssence(actor, toGrant, target);

                FoodData food = actor.getFoodData();
                int before = food.getFoodLevel();
                food.eat((int) toGrant, BloodBalance.bloodSaturationModifier());
                foodApplied = food.getFoodLevel() - before;
                MysticNetwork.syncVampireToClient(actor);
            }
        }

        NeoForge.EVENT_BUS.post(new BloodDrainTickEvent(
            actor, target,
            bloodData.isEmpty() ? 0f : BloodBalance.drainAmountPerInterval(),
            foodApplied, bloodData.isEmpty()));
    }

    @Override
    public void onComplete(LivingEntity target, ServerPlayer actor) {
        drainAccumulator.remove(actor.getUUID());
        drainedThisSession.remove(actor.getUUID());

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
        drainAccumulator.remove(actor.getUUID());
        drainedThisSession.remove(actor.getUUID());

        if ("cancel".equals(reason)) {
            NeoForge.EVENT_BUS.post(new BloodDrainCancelEvent(actor, target));
        } else {
            NeoForge.EVENT_BUS.post(new BloodDrainInterruptedEvent(actor, target, reason));
        }
        MysticNetwork.syncDrainToClient(actor);
    }

    public static void clearAccumulator(UUID playerId) {
        drainAccumulator.remove(playerId);
        drainedThisSession.remove(playerId);
    }
}
