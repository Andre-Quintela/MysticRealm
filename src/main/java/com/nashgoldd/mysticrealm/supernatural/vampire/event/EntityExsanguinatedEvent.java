package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class EntityExsanguinatedEvent extends Event {

    private final LivingEntity entity;
    private final ServerPlayer vampire;

    public EntityExsanguinatedEvent(LivingEntity entity, ServerPlayer vampire) {
        this.entity = entity;
        this.vampire = vampire;
    }

    public LivingEntity getEntity()  { return entity; }
    public ServerPlayer getVampire() { return vampire; }
}
