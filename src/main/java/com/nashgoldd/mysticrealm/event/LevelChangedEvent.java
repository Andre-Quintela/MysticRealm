package com.nashgoldd.mysticrealm.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Disparado no NeoForge.EVENT_BUS quando o level sobrenatural de um jogador é alterado.
 * Listeners futuros podem usar este evento para desbloquear habilidades ou aplicar buffs por nível.
 */
public class LevelChangedEvent extends Event {

    private final Player player;
    private final int oldLevel;
    private final int newLevel;

    public LevelChangedEvent(Player player, int oldLevel, int newLevel) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Player getPlayer() {
        return player;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
