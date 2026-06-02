package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class VampireTransformEvent extends Event {

    private final Player player;
    private final RaceType previousRace;

    public VampireTransformEvent(Player player, RaceType previousRace) {
        this.player = player;
        this.previousRace = previousRace;
    }

    public Player getPlayer() {
        return player;
    }

    public RaceType getPreviousRace() {
        return previousRace;
    }
}
