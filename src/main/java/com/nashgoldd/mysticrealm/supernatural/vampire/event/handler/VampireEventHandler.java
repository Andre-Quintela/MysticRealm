package com.nashgoldd.mysticrealm.supernatural.vampire.event.handler;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.network.MysticDamageTypes;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityRegistry;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityWheelData;
import com.nashgoldd.mysticrealm.supernatural.vampire.ability.NightVisionAbility;
import com.nashgoldd.mysticrealm.supernatural.vampire.ability.SpeedAbility;
import com.nashgoldd.mysticrealm.registry.MysticEffects;
import com.nashgoldd.mysticrealm.supernatural.transformation.IPendingTransformation;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.EntityBloodData;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.balance.BloodBalance;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodPoolChangedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodRegeneratedEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.feeding.BloodDrainAction;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireNearDeathEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireProgressionService;
import com.nashgoldd.mysticrealm.supernatural.vampire.registry.VampireWeaknessRegistry;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VampireEventHandler {

    private final Set<UUID> infectedPlayers = new HashSet<>();

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return;

        if (!VampireService.isVampire(sp)) {
            tickInfection(sp);
            return;
        }

        VampireData data = VampireService.getData(player);

        // Tick do sistema de canalização (drenagem de sangue, etc.)
        ChannelService.tick(sp);
        if (sp.tickCount % 5 == 0) {
            MysticNetwork.syncDrainToClient(sp);
        }

        VampireProgressionService.tickAge(sp);
        tickSunlight(sp, data, level);
        tickPassiveEffects(sp);
        tickNearDeath(sp, data);
    }

    private void tickInfection(ServerPlayer player) {
        UUID uuid = player.getUUID();
        MobEffectInstance infection = player.getEffect(MysticEffects.VAMPIRE_INFECTION);

        if (infection != null) {
            infectedPlayers.add(uuid);
            // Aviso quando restar menos de 30 segundos
            if (infection.getDuration() <= 600 && infection.getDuration() > 590) {
                player.sendSystemMessage(Component.translatable("mysticrealm.vampire.infection.fading"));
            }
        } else if (infectedPlayers.remove(uuid)) {
            // Efeito acabou naturalmente — o jogador sobreviveu ao tempo limite
            if (MysticEffects.VAMPIRE_INFECTION.get() instanceof IPendingTransformation pending) {
                pending.onExpire(player);
            }
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

    private void tickPassiveEffects(ServerPlayer player) {
        int blood = player.getFoodData().getFoodLevel();
        int regenThreshold = MysticConfig.VAMPIRE_REGENERATION_THRESHOLD.get();
        int speedThreshold = MysticConfig.VAMPIRE_SPEED_THRESHOLD.get();

        // Visão noturna — gerenciada passivamente a menos que esteja num slot da roda
        if (!isManagedByWheel(player, NightVisionAbility.ID)) {
            ensureEffect(player, MobEffects.NIGHT_VISION, 3600, 0, 220);
        }

        // Regeneração quando sangue alto (nunca na roda, sempre passivo)
        if (blood >= regenThreshold) {
            ensureEffect(player, MobEffects.REGENERATION, 200, 0);
        } else {
            player.removeEffect(MobEffects.REGENERATION);
        }

        // Velocidade quando sangue moderado — gerenciada passivamente a menos que esteja na roda
        if (!isManagedByWheel(player, SpeedAbility.ID)) {
            if (blood >= speedThreshold) {
                ensureEffect(player, MobEffects.SPEED, 200, 0);
            } else {
                player.removeEffect(MobEffects.SPEED);
            }
        }

        applyBloodPenalties(player, blood, regenThreshold, speedThreshold);
    }

    private boolean isManagedByWheel(ServerPlayer player, String abilityId) {
        AbilityWheelData wheelData = player.getData(MysticAttachments.ABILITY_WHEEL);
        return wheelData.getSlots().containsValue(abilityId);
    }

    private void applyBloodPenalties(ServerPlayer player, int blood, int regenThreshold, int speedThreshold) {
        int halfRegen = regenThreshold / 2;

        if (blood >= regenThreshold) {
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.SLOWNESS);
        } else if (blood >= speedThreshold) {
            ensureEffect(player, MobEffects.WEAKNESS, 200, 0);
            player.removeEffect(MobEffects.SLOWNESS);
        } else if (blood >= halfRegen) {
            ensureEffect(player, MobEffects.WEAKNESS, 200, 0);
            ensureEffect(player, MobEffects.SLOWNESS, 200, 0);
        } else {
            ensureEffect(player, MobEffects.WEAKNESS, 200, 1);
            ensureEffect(player, MobEffects.SLOWNESS, 200, 1);
        }
    }

    private float calculateSunlightDamage(ServerPlayer player) {
        VampireRank rank = VampireService.getData(player).getRank();
        float maxHealth = player.getMaxHealth();
        int maxOrdinal = VampireRank.values().length - 1; // 6 (BLOOD_SOVEREIGN)

        if (rank.ordinal() == 0) {
            return maxHealth * 10f; // NEWBORN = morte instantânea
        }

        float t = (float) rank.ordinal() / maxOrdinal;
        float maxSurvivalSeconds = MysticConfig.VAMPIRE_SUNLIGHT_MAX_SURVIVAL_SECONDS.get().floatValue();
        float survivalSeconds = 0.05f + t * (maxSurvivalSeconds - 0.05f);
        return maxHealth / survivalSeconds;
    }

    private void tickNearDeath(ServerPlayer player, VampireData data) {
        if (!data.isNearDeath()) return;
        if (!player.hasEffect(MobEffects.SLOWNESS) && !player.hasEffect(MobEffects.WEAKNESS)) {
            data.setNearDeath(false);
            MysticNetwork.syncVampireToClient(player);
        }
    }

    private void ensureEffect(Player player, Holder<MobEffect> effect, int minDuration, int amplifier) {
        ensureEffect(player, effect, minDuration, amplifier, 100);
    }

    private void ensureEffect(Player player, Holder<MobEffect> effect, int minDuration, int amplifier, int refreshThreshold) {
        MobEffectInstance current = player.getEffect(effect);
        if (current == null || current.getDuration() < refreshThreshold) {
            player.addEffect(new MobEffectInstance(effect, minDuration, amplifier, true, false));
        }
    }

    // Bloquear ingestão de alimentos comuns para vampiros
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!VampireService.isVampire(player)) return;
        ItemStack stack = event.getItemStack();
        if (stack.has(DataComponents.FOOD)) {
            event.setCanceled(true);
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(Component.literal("§4Vampiros não podem consumir alimentos comuns."));
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Transformação pendente — jogador ainda não é vampiro mas tem efeito de infecção
        if (!VampireService.isVampire(player)) {
            for (MobEffectInstance instance : player.getActiveEffects()) {
                if (instance.getEffect().value() instanceof IPendingTransformation pending) {
                    boolean isHardcore = player.level() instanceof ServerLevel sl
                        && sl.getLevelData().isHardcore();
                    if (isHardcore && !MysticConfig.ENABLE_HARDCORE_TRANSFORMATION.get()) return;
                    event.setCanceled(true);
                    infectedPlayers.remove(player.getUUID());
                    pending.applyTransformation(player, event.getSource());
                    return;
                }
            }
            return;
        }

        // Imortalidade vampírica
        if (!MysticConfig.VAMPIRE_IMMORTALITY_ENABLED.get()) return;

        if (VampireWeaknessRegistry.isLethalToVampire(event.getSource())) return;

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

    // Interrompe drenagem ativa quando o vampiro toma dano
    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!VampireService.isVampire(player)) return;
        if (ChannelService.getActive(player).isEmpty()) return;
        ChannelService.interrupt(player, "damaged");
        MysticNetwork.syncDrainToClient(player);
    }

    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide()) return;
        if (entity instanceof Player) return;
        if (!entity.hasData(MysticAttachments.ENTITY_BLOOD)) return;

        EntityBloodData data = entity.getData(MysticAttachments.ENTITY_BLOOD);
        if (!data.isInitialized() || data.isFull()) return;

        if (entity.tickCount % BloodBalance.bloodRegenIntervalTicks() != 0) return;

        float old = data.getCurrentBlood();
        data.regenerate(data.getMaxBlood() * BloodBalance.bloodRegenFraction());
        entity.setData(MysticAttachments.ENTITY_BLOOD, data);
        float gained = data.getCurrentBlood() - old;

        if (gained > 0f) {
            NeoForge.EVENT_BUS.post(new BloodRegeneratedEvent(entity, gained, data.getCurrentBlood()));
            NeoForge.EVENT_BUS.post(new BloodPoolChangedEvent(
                entity, old, data.getCurrentBlood(), data.getMaxBlood(),
                BloodPoolChangedEvent.ChangeReason.REGENERATED));
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChannelService.clearOnDisconnect(player);
        BloodDrainAction.clearAccumulator(player.getUUID());
        infectedPlayers.remove(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncToClient(player);
        MysticNetwork.syncVampireToClient(player);
        MysticNetwork.syncVampireProgressionToClient(player);

        // Desativar abilities ativas e limpar o estado ao morrer
        AbilityWheelData wheelData = player.getData(MysticAttachments.ABILITY_WHEEL);
        for (String id : new java.util.HashSet<>(wheelData.getActiveAbilities())) {
            AbilityRegistry.get(id).ifPresent(a -> a.deactivate(player));
        }
        wheelData.clearAllActive();
        MysticNetwork.syncAbilityDataToClient(player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncVampireToClient(player);
        MysticNetwork.syncVampireProgressionToClient(player);
        MysticNetwork.syncAbilityDataToClient(player);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MysticNetwork.syncVampireToClient(player);
        MysticNetwork.syncVampireProgressionToClient(player);
    }
}
