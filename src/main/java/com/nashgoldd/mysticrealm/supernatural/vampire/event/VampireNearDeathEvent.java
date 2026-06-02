package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class VampireNearDeathEvent extends Event {

    private final Player player;
    private final DamageSource damageSource;

    public VampireNearDeathEvent(Player player, DamageSource damageSource) {
        this.player = player;
        this.damageSource = damageSource;
    }

    public Player getPlayer() {
        return player;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }
}
