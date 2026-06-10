package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.DrainableEntityRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.CancelBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.network.RequestBloodDrainPacket;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public final class VampireClientInputHandler {

    private VampireClientInputHandler() {}

    // true quando a drenagem parou enquanto a tecla ainda estava pressionada —
    // exige que o player solte e re-pressione para drenar novamente (evita dano acidental)
    private static boolean requireKeyRelease = false;
    private static boolean prevWasDraining   = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!VampireService.isVampire(mc.player)) return;

        // ── Roda de Habilidades ───────────────────────────────────────────────
        boolean wheelDown = VampireKeyBindings.KEY_ABILITY_WHEEL.isDown();
        boolean wheelOpen = VampireAbilityWheelHud.isOpen();

        if (!wheelDown) {
            VampireAbilityWheelHud.clearSuppress();
        }

        if (wheelDown && !wheelOpen && !VampireAbilityWheelHud.isSuppressed()) {
            VampireAbilityWheelHud.openWheel();
        } else if (!wheelDown && wheelOpen) {
            VampireAbilityWheelHud.closeWheel();
        }

        if (VampireAbilityWheelHud.isOpen()) {
            VampireAbilityWheelHud.trackMouse();
            return;
        }

        // ── Drenagem de Sangue ────────────────────────────────────────────────
        // Guard: não processar drenagem enquanto qualquer tela estiver aberta
        if (mc.screen != null) return;

        // Contagem regressiva local do cooldown (atualizada pelo servidor via SyncDrainStatePacket)
        if (ClientDrainState.cooldownTicks > 0) {
            ClientDrainState.cooldownTicks--;
        }

        boolean keyDown = VampireKeyBindings.KEY_DRAIN_BLOOD.isDown();

        // Se a drenagem parou enquanto a tecla ainda está pressionada, exige re-pressionar
        if (prevWasDraining && !ClientDrainState.isDraining && keyDown) {
            requireKeyRelease = true;
        }
        if (!keyDown) {
            requireKeyRelease = false;
        }
        prevWasDraining = ClientDrainState.isDraining;

        if (keyDown && !requireKeyRelease && !ClientDrainState.isDraining && ClientDrainState.cooldownTicks <= 0) {
            // Detectar alvo pelo crosshair (validação completa ocorre no servidor)
            if (mc.crosshairPickEntity instanceof LivingEntity target
                    && DrainableEntityRegistry.isValidTarget(target, mc.player)
                    && mc.player.distanceTo(target) <= 2.5) {
                ClientPacketDistributor.sendToServer(new RequestBloodDrainPacket(target.getId()));
            }
        } else if (!keyDown && ClientDrainState.isDraining) {
            ClientPacketDistributor.sendToServer(new CancelBloodDrainPacket());
        }
    }
}
