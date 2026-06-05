package com.nashgoldd.mysticrealm.supernatural.channeling;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ChannelService {

    private static final Map<UUID, ChannelState> ACTIVE = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> COOLDOWNS = new HashMap<>();

    private ChannelService() {}

    public static boolean start(ServerPlayer player, LivingEntity target, ChannelAction action) {
        UUID id = player.getUUID();
        if (ACTIVE.containsKey(id)) return false;
        if (getCooldown(player, action.getActionId()) > 0) return false;

        ChannelState state = new ChannelState(action, target.getId());
        ACTIVE.put(id, state);
        action.onStart(target, player);
        return true;
    }

    // Cancelamento explícito pelo jogador (tecla solta)
    public static void cancel(ServerPlayer player) {
        doInterrupt(player, "cancel");
    }

    // Interrupção forçada pelo servidor (dano, alvo morreu, etc.)
    public static void interrupt(ServerPlayer player, String reason) {
        doInterrupt(player, reason);
    }

    private static void doInterrupt(ServerPlayer player, String reason) {
        ChannelState state = ACTIVE.remove(player.getUUID());
        if (state == null) return;
        LivingEntity target = resolveTarget(player, state);
        state.action.onInterrupt(target, player, reason);
    }

    public static void tick(ServerPlayer player) {
        UUID id = player.getUUID();

        // Decrementar cooldowns
        Map<String, Integer> cds = COOLDOWNS.get(id);
        if (cds != null) {
            cds.replaceAll((k, v) -> v - 1);
            cds.entrySet().removeIf(e -> e.getValue() <= 0);
            if (cds.isEmpty()) COOLDOWNS.remove(id);
        }

        ChannelState state = ACTIVE.get(id);
        if (state == null) return;

        LivingEntity target = resolveTarget(player, state);
        if (target == null) {
            doInterrupt(player, "target_gone");
            return;
        }

        if (target.isDeadOrDying()
                || player.distanceTo(target) > 2.5
                || !isLookingAt(player, target)) {
            doInterrupt(player, "condition_failed");
            return;
        }

        state.ticksElapsed++;
        state.action.onTick(target, player, state.ticksElapsed);

        if (state.ticksElapsed >= state.action.getDurationTicks()) {
            ACTIVE.remove(id);
            COOLDOWNS.computeIfAbsent(id, k -> new HashMap<>())
                .put(state.action.getActionId(), state.action.getCooldownTicks());
            state.action.onComplete(target, player);
        }
    }

    public static Optional<ChannelState> getActive(ServerPlayer player) {
        return Optional.ofNullable(ACTIVE.get(player.getUUID()));
    }

    public static int getCooldown(ServerPlayer player, String actionId) {
        return COOLDOWNS.getOrDefault(player.getUUID(), Map.of()).getOrDefault(actionId, 0);
    }

    // Limpa estado ao desconectar para não vazar memória entre sessões
    public static void clearOnDisconnect(ServerPlayer player) {
        UUID id = player.getUUID();
        ACTIVE.remove(id);
        COOLDOWNS.remove(id);
    }

    private static LivingEntity resolveTarget(ServerPlayer player, ChannelState state) {
        Entity entity = ((ServerLevel) player.level()).getEntity(state.targetEntityId);
        return entity instanceof LivingEntity le ? le : null;
    }

    private static boolean isLookingAt(ServerPlayer player, LivingEntity target) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 toTarget = target.getEyePosition()
            .subtract(player.getEyePosition()).normalize();
        return look.dot(toTarget) >= 0.85;
    }
}
