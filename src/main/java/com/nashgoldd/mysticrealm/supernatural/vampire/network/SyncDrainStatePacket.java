package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncDrainStatePacket(
    boolean draining,
    int ticksElapsed,
    int totalTicks,
    int cooldownTicks,
    float targetBloodCurrent,
    float targetBloodMax
) implements CustomPacketPayload {

    public static final Type<SyncDrainStatePacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_drain_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDrainStatePacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncDrainStatePacket decode(RegistryFriendlyByteBuf buf) {
                return new SyncDrainStatePacket(
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readInt(),
                    buf.readFloat(),
                    buf.readFloat()
                );
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncDrainStatePacket packet) {
                buf.writeBoolean(packet.draining());
                buf.writeInt(packet.ticksElapsed());
                buf.writeInt(packet.totalTicks());
                buf.writeInt(packet.cooldownTicks());
                buf.writeFloat(packet.targetBloodCurrent());
                buf.writeFloat(packet.targetBloodMax());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
