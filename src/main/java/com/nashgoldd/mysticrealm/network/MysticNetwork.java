package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class MysticNetwork {

    private MysticNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        registrar.playToClient(
            SyncPlayerDataPacket.TYPE,
            SyncPlayerDataPacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncPlayerData
        );

        registrar.playToClient(
            SyncVampireDataPacket.TYPE,
            SyncVampireDataPacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncVampireData
        );
    }

    public static void syncToClient(ServerPlayer player) {
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        PacketDistributor.sendToPlayer(player, new SyncPlayerDataPacket(
            data.getRace(),
            data.getLevel(),
            data.getExperience()
        ));
    }

    public static void syncVampireToClient(ServerPlayer player) {
        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        PacketDistributor.sendToPlayer(player, new SyncVampireDataPacket(
            data.isTransformed(),
            data.isSunlightBurning(),
            data.isNearDeath()
        ));
    }
}
