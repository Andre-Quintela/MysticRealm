package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelState;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.BloodDrainAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.CancelBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.RequestBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncDrainStatePacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public final class MysticNetwork {

    private MysticNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        // Servidor → Cliente
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
        registrar.playToClient(
            SyncDrainStatePacket.TYPE,
            SyncDrainStatePacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncDrainState
        );

        // Cliente → Servidor
        registrar.playToServer(
            RequestBloodDrainPacket.TYPE,
            RequestBloodDrainPacket.STREAM_CODEC,
            ServerPacketHandlers::handleRequestDrain
        );
        registrar.playToServer(
            CancelBloodDrainPacket.TYPE,
            CancelBloodDrainPacket.STREAM_CODEC,
            ServerPacketHandlers::handleCancelDrain
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

    public static void syncDrainToClient(ServerPlayer player) {
        Optional<ChannelState> active = ChannelService.getActive(player);
        int cooldown = ChannelService.getCooldown(player, BloodDrainAction.ID);

        SyncDrainStatePacket packet;
        if (active.isPresent()) {
            ChannelState state = active.get();
            packet = new SyncDrainStatePacket(
                true,
                state.ticksElapsed,
                state.action.getDurationTicks(),
                cooldown
            );
        } else {
            packet = new SyncDrainStatePacket(false, 0, BloodDrainAction.INSTANCE.getDurationTicks(), cooldown);
        }

        PacketDistributor.sendToPlayer(player, packet);
    }
}
