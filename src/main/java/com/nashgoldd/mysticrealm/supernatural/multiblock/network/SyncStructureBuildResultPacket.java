package com.nashgoldd.mysticrealm.supernatural.multiblock.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockBuildResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Servidor -> Cliente: resultado de uma tentativa de construção automática de estrutura
 * multiblock, usado para exibir feedback (sucesso, itens faltando, vida insuficiente, etc.)
 * na tela do controlador.
 */
public record SyncStructureBuildResultPacket(
    BlockPos controllerPos,
    boolean success,
    int reasonOrdinal,
    List<ItemStack> missingItems,
    float healthCost
) implements CustomPacketPayload {

    public static final Type<SyncStructureBuildResultPacket> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sync_structure_build_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStructureBuildResultPacket> STREAM_CODEC =
        new StreamCodec<>() {
            private final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> ITEM_LIST_CODEC =
                ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC);

            @Override
            public SyncStructureBuildResultPacket decode(RegistryFriendlyByteBuf buf) {
                return new SyncStructureBuildResultPacket(
                    buf.readBlockPos(),
                    buf.readBoolean(),
                    buf.readVarInt(),
                    ITEM_LIST_CODEC.decode(buf),
                    buf.readFloat()
                );
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SyncStructureBuildResultPacket packet) {
                buf.writeBlockPos(packet.controllerPos());
                buf.writeBoolean(packet.success());
                buf.writeVarInt(packet.reasonOrdinal());
                ITEM_LIST_CODEC.encode(buf, packet.missingItems());
                buf.writeFloat(packet.healthCost());
            }
        };

    public static SyncStructureBuildResultPacket from(BlockPos controllerPos, MultiblockBuildResult result) {
        return new SyncStructureBuildResultPacket(
            controllerPos,
            result.success(),
            result.reason().ordinal(),
            result.missingItems(),
            result.healthCost()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
