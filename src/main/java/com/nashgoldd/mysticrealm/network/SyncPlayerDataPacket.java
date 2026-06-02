package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Pacote servidor → cliente que sincroniza os dados sobrenaturais do jogador.
 * Enviado automaticamente no login, morte e mudança de dimensão.
 *
 * Usa a API moderna CustomPacketPayload do NeoForge (substituto do SimpleChannel legado).
 * O StreamCodec lida com serialização/desserialização de forma tipada e sem reflexão.
 *
 * Nota: Identifier.fromNamespaceAndPath() é a API correta no MC 26.x.
 */
public record SyncPlayerDataPacket(RaceType race, int level, long experience)
        implements CustomPacketPayload {

    public static final Type<SyncPlayerDataPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_player_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerDataPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncPlayerDataPacket decode(RegistryFriendlyByteBuf buf) {
                RaceType race = RaceType.valueOf(buf.readUtf().toUpperCase());
                int level = buf.readVarInt();
                long experience = buf.readLong();
                return new SyncPlayerDataPacket(race, level, experience);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncPlayerDataPacket packet) {
                buf.writeUtf(packet.race().name());
                buf.writeVarInt(packet.level());
                buf.writeLong(packet.experience());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
