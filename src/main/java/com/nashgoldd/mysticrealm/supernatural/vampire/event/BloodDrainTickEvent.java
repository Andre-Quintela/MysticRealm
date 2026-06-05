package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class BloodDrainTickEvent extends Event {

    private final ServerPlayer vampire;
    private final LivingEntity target;
    private final float bloodDrained;
    private final int foodApplied;
    private final boolean exsanguinating;

    public BloodDrainTickEvent(ServerPlayer vampire, LivingEntity target,
                               float bloodDrained, int foodApplied, boolean exsanguinating) {
        this.vampire = vampire;
        this.target = target;
        this.bloodDrained = bloodDrained;
        this.foodApplied = foodApplied;
        this.exsanguinating = exsanguinating;
    }

    public ServerPlayer getVampire()    { return vampire; }
    public LivingEntity getTarget()     { return target; }
    public float getBloodDrained()      { return bloodDrained; }
    public int getFoodApplied()         { return foodApplied; }
    public boolean isExsanguinating()   { return exsanguinating; }
}
