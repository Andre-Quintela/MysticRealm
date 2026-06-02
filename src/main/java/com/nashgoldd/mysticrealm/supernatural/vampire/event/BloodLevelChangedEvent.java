package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class BloodLevelChangedEvent extends Event {

    private final Player player;
    private final int oldBlood;
    private final int newBlood;

    public BloodLevelChangedEvent(Player player, int oldBlood, int newBlood) {
        this.player = player;
        this.oldBlood = oldBlood;
        this.newBlood = newBlood;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldBlood() {
        return oldBlood;
    }

    public int getNewBlood() {
        return newBlood;
    }
}
