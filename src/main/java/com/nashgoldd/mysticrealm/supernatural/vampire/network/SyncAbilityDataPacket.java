package com.nashgoldd.mysticrealm.supernatural.vampire.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncAbilityDataPacket(
    Map<Integer, String> slots,
    Set<String> activeAbilities
) implements CustomPacketPayload {

    public static final Type<SyncAbilityDataPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_ability_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAbilityDataPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncAbilityDataPacket decode(RegistryFriendlyByteBuf buf) {
                int slotCount = buf.readByte();
                Map<Integer, String> slots = new HashMap<>();
                for (int i = 0; i < slotCount; i++) {
                    slots.put((int) buf.readByte(), buf.readUtf());
                }
                int activeCount = buf.readByte();
                Set<String> active = new HashSet<>();
                for (int i = 0; i < activeCount; i++) {
                    active.add(buf.readUtf());
                }
                return new SyncAbilityDataPacket(slots, active);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncAbilityDataPacket pkt) {
                buf.writeByte(pkt.slots().size());
                pkt.slots().forEach((slot, id) -> {
                    buf.writeByte(slot);
                    buf.writeUtf(id);
                });
                buf.writeByte(pkt.activeAbilities().size());
                pkt.activeAbilities().forEach(buf::writeUtf);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
