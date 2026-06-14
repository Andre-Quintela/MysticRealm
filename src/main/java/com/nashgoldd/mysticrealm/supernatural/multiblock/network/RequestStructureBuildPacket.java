package com.nashgoldd.mysticrealm.supernatural.multiblock.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Cliente -> Servidor: solicita a construção automática das posições faltantes da estrutura
 * multiblock controlada pelo bloco em {@code controllerPos}.
 */
public record RequestStructureBuildPacket(BlockPos controllerPos) implements CustomPacketPayload {

    public static final Type<RequestStructureBuildPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "request_structure_build"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStructureBuildPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public RequestStructureBuildPacket decode(RegistryFriendlyByteBuf buf) {
                return new RequestStructureBuildPacket(buf.readBlockPos());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, RequestStructureBuildPacket packet) {
                buf.writeBlockPos(packet.controllerPos());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
