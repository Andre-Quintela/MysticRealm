package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireDataPacket;
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
            data.setLevelRaw(packet.level());
            data.setExperienceRaw(packet.experience());

            MysticRealmLogger.debug("Dados sobrenaturais sincronizados: race={}, level={}, xp={}",
                packet.race(), packet.level(), packet.experience());
        });
    }

    public static void handleSyncVampireData(SyncVampireDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
            data.setBloodLevelRaw(packet.bloodLevel());
            data.setMaxBloodRaw(packet.maxBlood());
            data.setTransformedRaw(packet.transformed());
            data.setSunlightBurningRaw(packet.sunlightBurning());
            data.setNearDeathRaw(packet.nearDeath());

            MysticRealmLogger.debug("Dados vampíricos sincronizados: blood={}/{}, transformed={}, nearDeath={}",
                packet.bloodLevel(), packet.maxBlood(), packet.transformed(), packet.nearDeath());
        });
    }
}
