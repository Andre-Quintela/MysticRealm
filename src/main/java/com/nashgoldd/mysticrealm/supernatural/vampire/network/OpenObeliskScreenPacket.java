package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenObeliskScreenPacket() implements CustomPacketPayload {

    public static final Type<OpenObeliskScreenPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "open_obelisk_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenObeliskScreenPacket> STREAM_CODEC =
        StreamCodec.unit(new OpenObeliskScreenPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
