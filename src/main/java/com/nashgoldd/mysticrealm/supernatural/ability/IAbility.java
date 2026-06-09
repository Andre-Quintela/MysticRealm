package com.nashgoldd.mysticrealm.supernatural.ability;

import net.minecraft.server.level.ServerPlayer;

public interface IAbility {

    String getId();

    String getDisplayName();

    /** Cor ARGB da fatia na roda (ex: 0xFF44AAFF). */
    int getIconColor();

    void activate(ServerPlayer player);

    void deactivate(ServerPlayer player);
}
