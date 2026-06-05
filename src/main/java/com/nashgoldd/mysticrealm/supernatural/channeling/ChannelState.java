package com.nashgoldd.mysticrealm.supernatural.channeling;

public final class ChannelState {

    public final ChannelAction action;
    public final int targetEntityId;
    public int ticksElapsed;

    ChannelState(ChannelAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
        this.ticksElapsed = 0;
    }
}
