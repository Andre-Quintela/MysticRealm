package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityRegistry;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityWheelData;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.multiblock.IMultiblockController;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockBuildResult;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockBuilder;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidationResult;
import com.nashgoldd.mysticrealm.supernatural.multiblock.StructureRegistry;
import com.nashgoldd.mysticrealm.supernatural.multiblock.effect.MultiblockFeedbackEffects;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.RequestStructureBuildPacket;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.RequestStructureValidationPacket;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.SyncStructureBuildResultPacket;
import com.nashgoldd.mysticrealm.supernatural.multiblock.network.SyncStructureValidationPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.BloodDrainAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.DrainableEntityRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.CancelBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.RequestBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.ToggleAbilityPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPacketHandlers {

    private ServerPacketHandlers() {}

    public static void handleRequestDrain(RequestBloodDrainPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!VampireService.isVampire(player)) return;
            if (player.isCreative() || player.isSpectator()) return;

            Entity entity = player.level().getEntity(packet.entityId());
            if (!(entity instanceof LivingEntity target)) return;

            if (!DrainableEntityRegistry.isValidTarget(target, player)) {
                player.sendSystemMessage(Component.literal("§4Este alvo não pode ser drenado."));
                return;
            }
            if (player.distanceTo(target) > 2.5) return;
            if (ChannelService.getCooldown(player, BloodDrainAction.ID) > 0) {
                player.sendSystemMessage(Component.literal("§4Aguarde antes de drenar novamente."));
                return;
            }

            ChannelService.start(player, target, BloodDrainAction.INSTANCE);
        });
    }

    public static void handleCancelDrain(CancelBloodDrainPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ChannelService.cancel(player);
        });
    }

    public static void handleRequestStructureValidation(RequestStructureValidationPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            BlockEntity blockEntity = serverLevel.getBlockEntity(packet.controllerPos());
            if (!(blockEntity instanceof IMultiblockController controller)) return;

            MultiblockValidationResult result = controller.revalidate(serverLevel);
            PacketDistributor.sendToPlayer(player, SyncStructureValidationPacket.from(packet.controllerPos(), result));
            MultiblockFeedbackEffects.spawnFeedbackParticles(serverLevel, result);
        });
    }

    public static void handleRequestStructureBuild(RequestStructureBuildPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            BlockEntity blockEntity = serverLevel.getBlockEntity(packet.controllerPos());
            if (!(blockEntity instanceof IMultiblockController controller)) return;

            StructureRegistry.get(controller.getStructureId()).ifPresent(structure -> {
                MultiblockBuildResult result = MultiblockBuilder.tryBuild(
                    serverLevel, controller.getControllerPos(), structure.pattern(), player);

                controller.setCachedResult(result.validation());

                PacketDistributor.sendToPlayer(player,
                    SyncStructureValidationPacket.from(packet.controllerPos(), result.validation()));
                PacketDistributor.sendToPlayer(player,
                    SyncStructureBuildResultPacket.from(packet.controllerPos(), result));
            });
        });
    }

    public static void handleToggleAbility(ToggleAbilityPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (!VampireService.isVampire(player)) return;

            AbilityWheelData data = player.getData(MysticAttachments.ABILITY_WHEEL);
            data.getSlot(packet.slot()).ifPresent(id ->
                AbilityRegistry.get(id).ifPresent(ability -> {
                    boolean nowActive = !data.isActive(id);
                    data.setActive(id, nowActive);
                    if (nowActive) ability.activate(player);
                    else           ability.deactivate(player);
                    MysticNetwork.syncAbilityDataToClient(player);
                })
            );
        });
    }
}
