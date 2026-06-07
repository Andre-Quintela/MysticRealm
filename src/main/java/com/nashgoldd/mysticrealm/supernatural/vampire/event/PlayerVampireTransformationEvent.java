package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class PlayerVampireTransformationEvent extends Event implements ICancellableEvent {

    private final ServerPlayer player;
    private final DamageSource deathCause;

    public PlayerVampireTransformationEvent(ServerPlayer player, DamageSource deathCause) {
        this.player = player;
        this.deathCause = deathCause;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public DamageSource getDeathCause() {
        return deathCause;
    }
}
