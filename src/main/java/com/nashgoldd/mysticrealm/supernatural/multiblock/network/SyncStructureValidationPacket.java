package com.nashgoldd.mysticrealm.supernatural.multiblock.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor -> Cliente: resultado da validação de uma estrutura multiblock, usado para
 * desenhar o feedback visual (outlines de blocos corretos/incorretos).
 */
public record SyncStructureValidationPacket(
    BlockPos controllerPos,
    boolean valid,
    List<BlockPos> matchedBlocks,
    List<BlockPos> missingBlocks,
    List<BlockPos> wrongBlocks,
    float percentCompleted
) implements CustomPacketPayload {

    public static final Type<SyncStructureValidationPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_structure_validation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStructureValidationPacket> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public SyncStructureValidationPacket decode(RegistryFriendlyByteBuf buf) {
                return new SyncStructureValidationPacket(
                    buf.readBlockPos(),
                    buf.readBoolean(),
                    readPositions(buf),
                    readPositions(buf),
                    readPositions(buf),
                    buf.readFloat()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncStructureValidationPacket packet) {
                buf.writeBlockPos(packet.controllerPos());
                buf.writeBoolean(packet.valid());
                writePositions(buf, packet.matchedBlocks());
                writePositions(buf, packet.missingBlocks());
                writePositions(buf, packet.wrongBlocks());
                buf.writeFloat(packet.percentCompleted());
            }

            private List<BlockPos> readPositions(RegistryFriendlyByteBuf buf) {
                int count = buf.readVarInt();
                List<BlockPos> positions = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    positions.add(buf.readBlockPos());
                }
                return positions;
            }

            private void writePositions(RegistryFriendlyByteBuf buf, List<BlockPos> positions) {
                buf.writeVarInt(positions.size());
                for (BlockPos pos : positions) {
                    buf.writeBlockPos(pos);
                }
            }
        };

    public static SyncStructureValidationPacket from(BlockPos controllerPos, MultiblockValidationResult result) {
        return new SyncStructureValidationPacket(
            controllerPos,
            result.valid(),
            result.matchedBlocks(),
            result.missingBlocks(),
            result.wrongBlocks(),
            result.percentCompleted()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
