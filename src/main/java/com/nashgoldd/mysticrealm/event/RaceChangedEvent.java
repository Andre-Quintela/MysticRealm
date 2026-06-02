package com.nashgoldd.mysticrealm.event;

import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Disparado no NeoForge.EVENT_BUS quando a raça de um jogador é alterada.
 * Listeners futuros podem usar este evento para aplicar efeitos, remover buffs, etc.
 */
public class RaceChangedEvent extends Event {

    private final Player player;
    private final RaceType oldRace;
    private final RaceType newRace;

    public RaceChangedEvent(Player player, RaceType oldRace, RaceType newRace) {
        this.player = player;
        this.oldRace = oldRace;
        this.newRace = newRace;
    }

    public Player getPlayer() {
        return player;
    }

    public RaceType getOldRace() {
        return oldRace;
    }

    public RaceType getNewRace() {
        return newRace;
    }
}
