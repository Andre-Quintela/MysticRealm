package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ToggleAbilityPacket(int slot) implements CustomPacketPayload {

    public static final Type<ToggleAbilityPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "toggle_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAbilityPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ToggleAbilityPacket decode(RegistryFriendlyByteBuf buf) {
                return new ToggleAbilityPacket(buf.readByte());
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, ToggleAbilityPacket pkt) {
                buf.writeByte(pkt.slot());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
