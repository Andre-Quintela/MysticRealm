package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class BloodRegeneratedEvent extends Event {

    private final LivingEntity entity;
    private final float amount;
    private final float newBlood;

    public BloodRegeneratedEvent(LivingEntity entity, float amount, float newBlood) {
        this.entity = entity;
        this.amount = amount;
        this.newBlood = newBlood;
    }

    public LivingEntity getEntity() { return entity; }
    public float getAmount()        { return amount; }
    public float getNewBlood()      { return newBlood; }
}
