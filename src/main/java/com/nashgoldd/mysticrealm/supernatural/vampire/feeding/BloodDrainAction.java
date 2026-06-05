package com.nashgoldd.mysticrealm.supernatural.vampire.feeding;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCancelEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainCompleteEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainInterruptedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodDrainStartEvent;
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

public final class BloodDrainAction implements ChannelAction {

    public static final BloodDrainAction INSTANCE = new BloodDrainAction();
    public static final String ID = "blood_drain";

    private BloodDrainAction() {}

    @Override public String getActionId()   { return ID; }
    @Override public int getDurationTicks() { return 60; }   // 3 segundos
    @Override public int getCooldownTicks() { return 100; }  // 5 segundos

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

        // Partículas de coração vermelho próximas ao pescoço da vítima
        sl.sendParticles(ParticleTypes.HEART,
            neck.x, neck.y, neck.z, 3, 0.15, 0.1, 0.15, 0.0);

        sl.playSound(null, actor.blockPosition(), SoundEvents.WITCH_DRINK,
            SoundSource.PLAYERS, 0.5f, 0.5f);
    }

    @Override
    public void onComplete(LivingEntity target, ServerPlayer actor) {
        // Restaurar sangue do vampiro (+4 food units = +20% sangue)
        FoodData food = actor.getFoodData();
        food.setFoodLevel(Math.min(20, food.getFoodLevel() + 4));

        // Consequências para a vítima
        ServerLevel sl = (ServerLevel) actor.level();
        if (target instanceof ServerPlayer sp) {
            sp.hurtServer(sl, sl.damageSources().playerAttack(actor), 2.0f);
        } else {
            target.hurt(sl.damageSources().playerAttack(actor), 2.0f);
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true));

        // Aldeões: penalidade adicional
        if (target.getType() == EntityType.VILLAGER || target.getType() == EntityType.WANDERING_TRADER) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0, false, true));
            sl.playSound(null, target.blockPosition(), SoundEvents.VILLAGER_HURT,
                SoundSource.NEUTRAL, 1.0f, 1.0f);
        }

        NeoForge.EVENT_BUS.post(new BloodDrainCompleteEvent(actor, target));
        MysticNetwork.syncVampireToClient(actor);
        MysticNetwork.syncDrainToClient(actor);
    }

    @Override
    public void onInterrupt(LivingEntity target, ServerPlayer actor, String reason) {
        if ("cancel".equals(reason)) {
            NeoForge.EVENT_BUS.post(new BloodDrainCancelEvent(actor, target));
        } else {
            NeoForge.EVENT_BUS.post(new BloodDrainInterruptedEvent(actor, target, reason));
        }
        MysticNetwork.syncDrainToClient(actor);
    }
}
