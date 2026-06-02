package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class VampireCuredEvent extends Event {

    private final Player player;

    public VampireCuredEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
