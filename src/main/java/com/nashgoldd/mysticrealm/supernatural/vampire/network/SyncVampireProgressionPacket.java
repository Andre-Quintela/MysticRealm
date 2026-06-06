package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SyncVampireProgressionPacket(
    VampireRank rank,
    long bloodEssence,
    long vampireAgeTicks,
    int ascensionCount
) implements CustomPacketPayload {

    public static final Type<SyncVampireProgressionPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_vampire_progression"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVampireProgressionPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncVampireProgressionPacket decode(RegistryFriendlyByteBuf buf) {
                return new SyncVampireProgressionPacket(
                    VampireRank.valueOf(buf.readUtf()),
                    buf.readLong(),
                    buf.readLong(),
                    buf.readInt()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncVampireProgressionPacket packet) {
                buf.writeUtf(packet.rank().name());
                buf.writeLong(packet.bloodEssence());
                buf.writeLong(packet.vampireAgeTicks());
                buf.writeInt(packet.ascensionCount());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
