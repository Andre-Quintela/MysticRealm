package com.nashgoldd.mysticrealm.supernatural.vampire.event;

import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class VampireAscensionEvent extends Event {

    private final ServerPlayer vampire;
    private final VampireRank newRank;
    private final int ascensionCount;

    public VampireAscensionEvent(ServerPlayer vampire, VampireRank newRank, int ascensionCount) {
        this.vampire = vampire;
        this.newRank = newRank;
        this.ascensionCount = ascensionCount;
    }

    public ServerPlayer getVampire()  { return vampire; }
    public VampireRank getNewRank()   { return newRank; }
    public int getAscensionCount()    { return ascensionCount; }
}
