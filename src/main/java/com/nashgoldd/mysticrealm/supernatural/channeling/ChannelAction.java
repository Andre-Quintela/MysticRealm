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

    // Retorna true para encerrar o canal de forma limpa sem aplicar cooldown.
    // Útil para ações que devem parar quando uma condição do alvo é atingida
    // (ex: pool de sangue esgotado) mas permitem reinício imediato.
    default boolean shouldSafeStop(LivingEntity target, ServerPlayer actor, int ticksElapsed) {
        return false;
    }
}
