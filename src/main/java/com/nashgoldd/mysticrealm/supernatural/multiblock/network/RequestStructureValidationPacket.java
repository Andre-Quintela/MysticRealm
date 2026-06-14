package com.nashgoldd.mysticrealm.supernatural.multiblock.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Cliente -> Servidor: solicita a (re)validação da estrutura multiblock controlada
 * pelo bloco em {@code controllerPos}.
 */
public record RequestStructureValidationPacket(BlockPos controllerPos) implements CustomPacketPayload {

    public static final Type<RequestStructureValidationPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "request_structure_validation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestStructureValidationPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public RequestStructureValidationPacket decode(RegistryFriendlyByteBuf buf) {
                return new RequestStructureValidationPacket(buf.readBlockPos());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, RequestStructureValidationPacket packet) {
                buf.writeBlockPos(packet.controllerPos());
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
