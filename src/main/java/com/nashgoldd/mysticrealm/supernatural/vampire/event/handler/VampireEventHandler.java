package com.nashgoldd.mysticrealm.supernatural.vampire.event.handler;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.network.MysticDamageTypes;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireNearDeathEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.registry.VampireWeaknessRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class VampireEventHandler {

    private static final int NEAR_DEATH_DEBUFF_TICK_INTERVAL = 40;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return;
        if (!VampireService.isVampire(player)) return;

        VampireData data = VampireService.getData(player);

        tickBloodDrain(sp, data);
        tickSunlight(sp, data, level);
        tickPassiveEffects(sp, data);
        tickNearDeath(sp, data);
    }

    private void tickBloodDrain(ServerPlayer player, VampireData data) {
        int intervalTicks = MysticConfig.VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS.get() * 20;
        if (intervalTicks <= 0) return;

        if (player.tickCount % intervalTicks == 0) {
            int drain = MysticConfig.VAMPIRE_BLOOD_DRAIN_AMOUNT.get();
            data.removeBlood(drain, player);
            MysticNetwork.syncVampireToClient(player);
            MysticRealmLogger.debug("Drenagem de sangue: -{} → {}", drain, data.getBloodLevel());
        }
    }

    private void tickSunlight(ServerPlayer player, VampireData data, Level level) {
        if (!MysticConfig.VAMPIRE_SUNLIGHT_DAMAGE_ENABLED.get()) {
            if (data.isSunlightBurning()) {
                data.setSunlightBurning(false);
                MysticNetwork.syncVampireToClient(player);
            }
            return;
        }

        boolean inSunlight = isInSunlight(player, level);
        boolean wasAlready = data.isSunlightBurning();

        if (inSunlight != wasAlready) {
            data.setSunlightBurning(inSunlight);
            MysticNetwork.syncVampireToClient(player);
        }

        // Aplica dano uma vez por segundo para evitar invulnerability frames cancelarem
        if (inSunlight && level instanceof ServerLevel sl && player.tickCount % 20 == 0) {
            player.hurtServer(sl, sl.damageSources().source(MysticDamageTypes.SUNLIGHT), calculateSunlightDamage(player));
        }
    }

    private boolean isInSunlight(Player player, Level level) {
        BlockPos pos = player.blockPosition();
        return level.dimensionType().hasSkyLight()
            && level.isBrightOutside()
            && level.canSeeSky(pos)
            && level.getRainLevel(1.0f) < 0.5f;
    }

    private void tickPassiveEffects(ServerPlayer player, VampireData data) {
        int blood = data.getBloodLevel();
        int regenThreshold = MysticConfig.VAMPIRE_REGENERATION_THRESHOLD.get();
        int speedThreshold = MysticConfig.VAMPIRE_SPEED_THRESHOLD.get();

        // Visão noturna — sempre ativa para vampiros
        ensureEffect(player, MobEffects.NIGHT_VISION, 400, 0);

        // Regeneração quando sangue alto
        if (blood >= regenThreshold) {
            ensureEffect(player, MobEffects.REGENERATION, 200, 0);
        } else {
            player.removeEffect(MobEffects.REGENERATION);
        }

        // Velocidade quando sangue moderado
        if (blood >= speedThreshold) {
            ensureEffect(player, MobEffects.SPEED, 200, 0);
        } else {
            player.removeEffect(MobEffects.SPEED);
        }

        // Penalidades por fome de sangue
        applyBloodPenalties(player, blood, regenThreshold, speedThreshold);
    }

    private void applyBloodPenalties(ServerPlayer player, int blood, int regenThreshold, int speedThreshold) {
        int halfRegen = regenThreshold / 2;

        if (blood >= regenThreshold) {
            // Sem penalidade
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.SLOWNESS);
        } else if (blood >= speedThreshold) {
            // Fraqueza leve
            ensureEffect(player, MobEffects.WEAKNESS, 200, 0);
            player.removeEffect(MobEffects.SLOWNESS);
        } else if (blood >= halfRegen) {
            // Fraqueza moderada
            ensureEffect(player, MobEffects.WEAKNESS, 200, 0);
            ensureEffect(player, MobEffects.SLOWNESS, 200, 0);
        } else {
            // Fraqueza severa
            ensureEffect(player, MobEffects.WEAKNESS, 200, 1);
            ensureEffect(player, MobEffects.SLOWNESS, 200, 1);
        }
    }

    private float calculateSunlightDamage(ServerPlayer player) {
        PlayerSupernaturalData sData = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        int vampireLevel = sData.getLevel();
        int maxLevel = MysticConfig.MAX_LEVEL.get();
        float maxHealth = player.getMaxHealth();

        if (vampireLevel <= 1 || maxLevel <= 1) {
            // Level 1: morte instantânea — dano muito acima da vida máxima
            return maxHealth * 10f;
        }

        // t = 0 no level 2, t = 1 no level máximo
        float t = (float)(vampireLevel - 1) / (maxLevel - 1);
        float maxSurvivalSeconds = MysticConfig.VAMPIRE_SUNLIGHT_MAX_SURVIVAL_SECONDS.get().floatValue();

        // Interpolação linear: 0.05s de sobrevivência no level 2 → maxSurvivalSeconds no level max
        float survivalSeconds = 0.05f + t * (maxSurvivalSeconds - 0.05f);
        return maxHealth / survivalSeconds;
    }

    private void tickNearDeath(ServerPlayer player, VampireData data) {
        if (!data.isNearDeath()) return;
        // near-death é zerado quando os efeitos de slowness/weakness expiram naturalmente
        // Verificamos aqui se os efeitos ainda estão ativos
        if (!player.hasEffect(MobEffects.SLOWNESS) && !player.hasEffect(MobEffects.WEAKNESS)) {
            data.setNearDeath(false);
            MysticNetwork.syncVampireToClient(player);
        }
    }

    private void ensureEffect(Player player, Holder<MobEffect> effect, int minDuration, int amplifier) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < 100) {
            player.addEffect(new MobEffectInstance(effect, minDuration, amplifier, true, false));
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!VampireService.isVampire(player)) return;
        if (!MysticConfig.VAMPIRE_IMMORTALITY_ENABLED.get()) return;

        // Fraquezas sobrenaturais podem matar normalmente
        if (VampireWeaknessRegistry.isLethalToVampire(event.getSource())) return;

        // Imortalidade: cancela a morte
        event.setCanceled(true);

        float minHealth = MysticConfig.VAMPIRE_MINIMUM_HEALTH.get().floatValue();
        player.setHealth(minHealth);

        int debuffDuration = MysticConfig.VAMPIRE_NEAR_DEATH_DEBUFF_DURATION.get();
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, debuffDuration, 3, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, debuffDuration, 3, false, true));

        VampireData data = VampireService.getData(player);
        data.setNearDeath(true);

        NeoForge.EVENT_BUS.post(new VampireNearDeathEvent(player, event.getSource()));
        MysticNetwork.syncVampireToClient(player);

        MysticRealmLogger.debug("Imortalidade vampírica ativada para {}", player.getName().getString());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncToClient(player);
        MysticNetwork.syncVampireToClient(player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncVampireToClient(player);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncVampireToClient(player);
    }
}
