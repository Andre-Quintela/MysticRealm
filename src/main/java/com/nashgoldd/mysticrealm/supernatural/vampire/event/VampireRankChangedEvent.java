package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VampireRankChangedEvent extends Event {

    private final ServerPlayer vampire;
    private final VampireRank previous;
    private final VampireRank newRank;

    public VampireRankChangedEvent(ServerPlayer vampire, VampireRank previous, VampireRank newRank) {
        this.vampire = vampire;
        this.previous = previous;
        this.newRank = newRank;
    }

    public ServerPlayer getVampire()   { return vampire; }
    public VampireRank getPrevious()   { return previous; }
    public VampireRank getNewRank()    { return newRank; }
}
