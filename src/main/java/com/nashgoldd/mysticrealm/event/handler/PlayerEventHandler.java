package com.nashgoldd.mysticrealm.event.handler;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerEventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MysticRealmLogger.debug("Sincronizando dados para jogador: {}", player.getName().getString());
            MysticNetwork.syncToClient(player);
            MysticNetwork.syncVampireToClient(player);
        }
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MysticRealmLogger.debug("Ressincronizando dados após mudança de dimensão: {}", player.getName().getString());
            MysticNetwork.syncToClient(player);
            MysticNetwork.syncVampireToClient(player);
        }
    }
}
