package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelState;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.EntityBloodData;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.balance.BloodBalance;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.BloodDrainAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.CancelBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.RequestBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncDrainStatePacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireDataPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.OpenObeliskScreenPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.SyncVampireProgressionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public final class MysticNetwork {

    private MysticNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        // Servidor → Cliente
        registrar.playToClient(
            SyncPlayerDataPacket.TYPE,
            SyncPlayerDataPacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncPlayerData
        );
        registrar.playToClient(
            SyncVampireDataPacket.TYPE,
            SyncVampireDataPacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncVampireData
        );
        registrar.playToClient(
            SyncDrainStatePacket.TYPE,
            SyncDrainStatePacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncDrainState
        );
        registrar.playToClient(
            SyncVampireProgressionPacket.TYPE,
            SyncVampireProgressionPacket.STREAM_CODEC,
            ClientPacketHandlers::handleSyncVampireProgression
        );
        registrar.playToClient(
            OpenObeliskScreenPacket.TYPE,
            OpenObeliskScreenPacket.STREAM_CODEC,
            ClientPacketHandlers::handleOpenObeliskScreen
        );

        // Cliente → Servidor
        registrar.playToServer(
            RequestBloodDrainPacket.TYPE,
            RequestBloodDrainPacket.STREAM_CODEC,
            ServerPacketHandlers::handleRequestDrain
        );
        registrar.playToServer(
            CancelBloodDrainPacket.TYPE,
            CancelBloodDrainPacket.STREAM_CODEC,
            ServerPacketHandlers::handleCancelDrain
        );
    }

    public static void syncToClient(ServerPlayer player) {
        PlayerSupernaturalData data = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        PacketDistributor.sendToPlayer(player, new SyncPlayerDataPacket(data.getRace()));
    }

    public static void syncVampireToClient(ServerPlayer player) {
        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        PacketDistributor.sendToPlayer(player, new SyncVampireDataPacket(
            data.isTransformed(),
            data.isSunlightBurning(),
            data.isNearDeath()
        ));
    }

    public static void syncVampireProgressionToClient(ServerPlayer player) {
        VampireData data = player.getData(MysticAttachments.VAMPIRE_DATA);
        PacketDistributor.sendToPlayer(player, new SyncVampireProgressionPacket(
            data.getRank(),
            data.getBloodEssence(),
            data.getVampireAgeTicks(),
            data.getAscensionCount()
        ));
    }

    public static void syncDrainToClient(ServerPlayer player) {
        Optional<ChannelState> active = ChannelService.getActive(player);
        int cooldown = ChannelService.getCooldown(player, BloodDrainAction.ID);

        SyncDrainStatePacket packet;
        if (active.isPresent()) {
            ChannelState state = active.get();

            float bloodCurrent = 0f;
            float bloodMax = 0f;
            ServerLevel sl = (ServerLevel) player.level();
            var target = sl.getEntity(state.targetEntityId);
            if (target instanceof LivingEntity le && le.hasData(MysticAttachments.ENTITY_BLOOD)) {
                EntityBloodData bd = le.getData(MysticAttachments.ENTITY_BLOOD);
                if (bd.isInitialized()) {
                    bloodCurrent = bd.getCurrentBlood();
                    bloodMax = bd.getMaxBlood();
                }
            }

            packet = new SyncDrainStatePacket(
                true,
                state.ticksElapsed,
                state.action.getDurationTicks(),
                cooldown,
                bloodCurrent,
                bloodMax
            );
        } else {
            float bloodCurrent = 0f;
            float bloodMax = 0f;
            LivingEntity hovered = findHoveredLivingEntity(player, 5.0);
            if (hovered != null) {
                if (hovered.hasData(MysticAttachments.ENTITY_BLOOD)) {
                    EntityBloodData bd = hovered.getData(MysticAttachments.ENTITY_BLOOD);
                    if (bd.isInitialized()) {
                        bloodCurrent = bd.getCurrentBlood();
                        bloodMax = bd.getMaxBlood();
                    }
                }
                // Entidade nunca drenada — exibe pool cheio calculado pela vida máxima
                if (bloodMax == 0f) {
                    bloodMax = BloodBalance.maxBloodForEntity(hovered);
                    bloodCurrent = bloodMax;
                }
            }
            packet = new SyncDrainStatePacket(false, 0, BloodDrainAction.INSTANCE.getDurationTicks(), cooldown, bloodCurrent, bloodMax);
        }

        PacketDistributor.sendToPlayer(player, packet);
    }

    private static LivingEntity findHoveredLivingEntity(ServerPlayer player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        AABB searchBox = new AABB(eyePos, eyePos).inflate(range);

        LivingEntity closest = null;
        double closestDist = range * range + 1.0;

        for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                le -> le != player && !le.isSpectator())) {
            Vec3 toEntity = e.getBoundingBox().getCenter().subtract(eyePos);
            double dist = toEntity.lengthSqr();
            if (dist >= closestDist) continue;
            if (toEntity.normalize().dot(lookVec) < 0.95) continue;

            // Verifica linha de visão — ignora entidades atrás de blocos
            Vec3 entityCenter = e.getBoundingBox().getCenter();
            BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePos, entityCenter,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (blockHit.getType() != HitResult.Type.MISS) continue;

            closest = e;
            closestDist = dist;
        }
        return closest;
    }
}
