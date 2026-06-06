package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncPlayerDataPacket(RaceType race)
        implements CustomPacketPayload {

    public static final Type<SyncPlayerDataPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_player_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerDataPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncPlayerDataPacket decode(RegistryFriendlyByteBuf buf) {
                RaceType race = RaceType.valueOf(buf.readUtf().toUpperCase());
                return new SyncPlayerDataPacket(race);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncPlayerDataPacket packet) {
                buf.writeUtf(packet.race().name());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
