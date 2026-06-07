package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VampireInfectionStartEvent extends Event {

    private final ServerPlayer player;

    public VampireInfectionStartEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
