package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.BloodDrainAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.DrainableEntityRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.CancelBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.RequestBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
}
