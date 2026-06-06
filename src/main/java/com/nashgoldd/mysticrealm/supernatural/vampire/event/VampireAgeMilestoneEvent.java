package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VampireAgeMilestoneEvent extends Event {

    private final ServerPlayer vampire;
    private final long ageTicks;
    private final long milestoneHours;

    public VampireAgeMilestoneEvent(ServerPlayer vampire, long ageTicks, long milestoneHours) {
        this.vampire = vampire;
        this.ageTicks = ageTicks;
        this.milestoneHours = milestoneHours;
    }

    public ServerPlayer getVampire()    { return vampire; }
    public long getAgeTicks()           { return ageTicks; }
    public long getMilestoneHours()     { return milestoneHours; }
}
