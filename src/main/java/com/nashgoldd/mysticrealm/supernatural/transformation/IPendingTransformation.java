package com.nashgoldd.mysticrealm.supernatural.transformation;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;

/**
 * Marker interface for MobEffects that represent a pending supernatural transformation.
 * When a player dies with an active effect implementing this interface, the death is
 * cancelled and applyTransformation() is called instead of processing normal death logic.
 *
 * Future uses: WerewolfCurseEffect, DemonicCorruptionEffect, etc.
 */
public interface IPendingTransformation {

    void applyTransformation(ServerPlayer player, DamageSource deathCause);

    void onExpire(ServerPlayer player);
}
