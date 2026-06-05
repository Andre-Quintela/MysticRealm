package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class BloodDrainStartEvent extends Event {

    private final ServerPlayer vampire;
    private final LivingEntity target;

    public BloodDrainStartEvent(ServerPlayer vampire, LivingEntity target) {
        this.vampire = vampire;
        this.target = target;
    }

    public ServerPlayer getVampire() { return vampire; }
    public LivingEntity getTarget()  { return target; }
}
