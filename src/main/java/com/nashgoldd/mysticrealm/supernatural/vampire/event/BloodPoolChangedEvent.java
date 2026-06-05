package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class BloodPoolChangedEvent extends Event {

    public enum ChangeReason { DRAINED, REGENERATED }

    private final LivingEntity entity;
    private final float oldBlood;
    private final float newBlood;
    private final float maxBlood;
    private final ChangeReason reason;

    public BloodPoolChangedEvent(LivingEntity entity, float oldBlood, float newBlood,
                                 float maxBlood, ChangeReason reason) {
        this.entity = entity;
        this.oldBlood = oldBlood;
        this.newBlood = newBlood;
        this.maxBlood = maxBlood;
        this.reason = reason;
    }

    public LivingEntity getEntity() { return entity; }
    public float getOldBlood()      { return oldBlood; }
    public float getNewBlood()      { return newBlood; }
    public float getMaxBlood()      { return maxBlood; }
    public ChangeReason getReason() { return reason; }
}
