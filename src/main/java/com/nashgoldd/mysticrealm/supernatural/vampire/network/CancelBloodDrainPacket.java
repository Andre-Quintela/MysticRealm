package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CancelBloodDrainPacket() implements CustomPacketPayload {

    public static final Type<CancelBloodDrainPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "cancel_blood_drain"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CancelBloodDrainPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public CancelBloodDrainPacket decode(RegistryFriendlyByteBuf buf) {
                return new CancelBloodDrainPacket();
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, CancelBloodDrainPacket packet) {
                // sem campos
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
