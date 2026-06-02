package com.nashgoldd.mysticrealm.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Disparado no NeoForge.EVENT_BUS quando a experiência sobrenatural de um jogador é alterada.
 * Listeners futuros podem usar este evento para calcular level-up automático.
 */
public class ExperienceChangedEvent extends Event {

    private final Player player;
    private final long oldExperience;
    private final long newExperience;

    public ExperienceChangedEvent(Player player, long oldExperience, long newExperience) {
        this.player = player;
        this.oldExperience = oldExperience;
        this.newExperience = newExperience;
    }

    public Player getPlayer() {
        return player;
    }

    public long getOldExperience() {
        return oldExperience;
    }

    public long getNewExperience() {
        return newExperience;
    }
}
