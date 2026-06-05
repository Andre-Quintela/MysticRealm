package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RequestBloodDrainPacket(int entityId) implements CustomPacketPayload {

    public static final Type<RequestBloodDrainPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "request_blood_drain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestBloodDrainPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public RequestBloodDrainPacket decode(RegistryFriendlyByteBuf buf) {
                return new RequestBloodDrainPacket(buf.readInt());
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, RequestBloodDrainPacket packet) {
                buf.writeInt(packet.entityId());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
