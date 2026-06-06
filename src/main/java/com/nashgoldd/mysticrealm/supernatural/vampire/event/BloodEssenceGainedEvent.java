package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

public class BloodEssenceGainedEvent extends Event {

    private final ServerPlayer vampire;
    private final long amount;
    private final long newTotal;
    private final LivingEntity source;

    public BloodEssenceGainedEvent(ServerPlayer vampire, long amount, long newTotal, LivingEntity source) {
        this.vampire = vampire;
        this.amount = amount;
        this.newTotal = newTotal;
        this.source = source;
    }

    public ServerPlayer getVampire() { return vampire; }
    public long getAmount()          { return amount; }
    public long getNewTotal()        { return newTotal; }
    public LivingEntity getSource()  { return source; }
}
