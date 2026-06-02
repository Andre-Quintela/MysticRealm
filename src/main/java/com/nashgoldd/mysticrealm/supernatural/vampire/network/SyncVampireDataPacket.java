package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncVampireDataPacket(
    int bloodLevel,
    int maxBlood,
    boolean transformed,
    boolean sunlightBurning,
    boolean nearDeath
) implements CustomPacketPayload {

    public static final Type<SyncVampireDataPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_vampire_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVampireDataPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncVampireDataPacket decode(RegistryFriendlyByteBuf buf) {
                return new SyncVampireDataPacket(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncVampireDataPacket packet) {
                buf.writeVarInt(packet.bloodLevel());
                buf.writeVarInt(packet.maxBlood());
                buf.writeBoolean(packet.transformed());
                buf.writeBoolean(packet.sunlightBurning());
                buf.writeBoolean(packet.nearDeath());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
