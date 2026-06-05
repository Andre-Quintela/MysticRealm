package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

// Disparado quando a drenagem é interrompida por condições externas
// (alvo morreu, saiu do alcance, vampiro tomou dano, etc.)
public class BloodDrainInterruptedEvent extends Event {

    private final ServerPlayer vampire;
    private final LivingEntity target;
    private final String reason;

    public BloodDrainInterruptedEvent(ServerPlayer vampire, LivingEntity target, String reason) {
        this.vampire = vampire;
        this.target = target;
        this.reason = reason;
    }

    public ServerPlayer getVampire() { return vampire; }
    public LivingEntity getTarget()  { return target; }
    public String getReason()        { return reason; }
}
