package com.nashgoldd.mysticrealm.supernatural.channeling;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public interface ChannelAction {

    String getActionId();

    int getDurationTicks();

    int getCooldownTicks();

    boolean isValidTarget(LivingEntity target, ServerPlayer actor);

    void onStart(LivingEntity target, ServerPlayer actor);

    void onTick(LivingEntity target, ServerPlayer actor, int ticksElapsed);

    void onComplete(LivingEntity target, ServerPlayer actor);

    void onInterrupt(LivingEntity target, ServerPlayer actor, String reason);
}
