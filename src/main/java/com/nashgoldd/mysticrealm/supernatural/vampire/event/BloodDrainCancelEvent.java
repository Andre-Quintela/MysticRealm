package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

// Disparado quando o vampiro cancela explicitamente (solta a tecla)
public class BloodDrainCancelEvent extends Event {

    private final ServerPlayer vampire;
    private final LivingEntity target;

    public BloodDrainCancelEvent(ServerPlayer vampire, LivingEntity target) {
        this.vampire = vampire;
        this.target = target;
    }

    public ServerPlayer getVampire() { return vampire; }
    public LivingEntity getTarget()  { return target; }
}
