package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.ClientDrainState;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.screen.VampireObeliskScreen;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.OpenObeliskScreenPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncDrainStatePacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireDataPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireProgressionPacket;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Handlers de pacotes executados exclusivamente no cliente.
 * Carregado de forma lazy — seguro referenciar em MysticNetwork via method reference.
 */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {}

    public static void handleSyncPlayerData(SyncPlayerDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
            data.setRaceRaw(packet.race());

            MysticRealmLogger.debug("Dados sobrenaturais sincronizados: race={}", packet.race());
        });
    }

    public static void handleSyncVampireData(SyncVampireDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            data.setTransformedRaw(packet.transformed());
            data.setSunlightBurningRaw(packet.sunlightBurning());
            data.setNearDeathRaw(packet.nearDeath());

            MysticRealmLogger.debug("Dados vampíricos sincronizados: transformed={}, sunlight={}, nearDeath={}",
                packet.transformed(), packet.sunlightBurning(), packet.nearDeath());
        });
    }

    public static void handleSyncVampireProgression(SyncVampireProgressionPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            data.setRankRaw(packet.rank());
            data.setBloodEssenceRaw(packet.bloodEssence());
            data.setVampireAgeTicksRaw(packet.vampireAgeTicks());
            data.setAscensionCountRaw(packet.ascensionCount());

            MysticRealmLogger.debug("Progressão vampírica sincronizada: rank={}, essence={}, ageTicks={}, ascensions={}",
                packet.rank(), packet.bloodEssence(), packet.vampireAgeTicks(), packet.ascensionCount());
        });
    }

    public static void handleOpenObeliskScreen(OpenObeliskScreenPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new VampireObeliskScreen()));
    }

    public static void handleSyncDrainState(SyncDrainStatePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientDrainState.isDraining          = packet.draining();
            ClientDrainState.ticksElapsed        = packet.ticksElapsed();
            ClientDrainState.totalTicks          = packet.totalTicks();
            ClientDrainState.cooldownTicks       = packet.cooldownTicks();
            ClientDrainState.targetBloodCurrent  = packet.targetBloodCurrent();
            ClientDrainState.targetBloodMax      = packet.targetBloodMax();
        });
    }
}
