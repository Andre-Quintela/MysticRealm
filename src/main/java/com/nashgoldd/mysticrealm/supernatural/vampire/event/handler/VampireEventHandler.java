package com.nashgoldd.mysticrealm.supernatural.vampire.event.handler;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.network.MysticDamageTypes;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.channeling.ChannelService;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireNearDeathEvent;
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
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class VampireEventHandler {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return;
        if (!VampireService.isVampire(player)) return;

        VampireData data = VampireService.getData(player);

        // Manter saturação zero — evita regeneração vanilla interferir com nossa lógica
        sp.getFoodData().setSaturation(0f);

        // Tick do sistema de canalização (drenagem de sangue, etc.)
        ChannelService.tick(sp);
        if (ChannelService.getActive(sp).isPresent() && sp.tickCount % 5 == 0) {
            MysticNetwork.syncDrainToClient(sp);
        }

        tickBloodDrain(sp);
        tickSunlight(sp, data, level);
        tickPassiveEffects(sp);
        tickNearDeath(sp, data);
    }

    private void tickBloodDrain(ServerPlayer player) {
        int intervalTicks = MysticConfig.VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS.get() * 20;
        if (intervalTicks <= 0) return;

        if (player.tickCount % intervalTicks == 0) {
            FoodData food = player.getFoodData();
            int drainUnits = Math.max(1, MysticConfig.VAMPIRE_BLOOD_DRAIN_AMOUNT.get() / 5);
            food.setFoodLevel(Math.max(0, food.getFoodLevel() - drainUnits));
            MysticNetwork.syncVampireToClient(player);
            MysticRealmLogger.debug("Drenagem de sangue: -{} food unit(s) → {}/20", drainUnits, food.getFoodLevel());
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
        // blood como percentual 0-100 derivado de FoodData (0-20 → 0-100)
        int blood = player.getFoodData().getFoodLevel() * 5;
        int regenThreshold = MysticConfig.VAMPIRE_REGENERATION_THRESHOLD.get();
        int speedThreshold = MysticConfig.VAMPIRE_SPEED_THRESHOLD.get();

        // Visão noturna — threshold 220 evita o piscar dos últimos 200 ticks
        ensureEffect(player, MobEffects.NIGHT_VISION, 3600, 0, 220);

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

        applyBloodPenalties(player, blood, regenThreshold, speedThreshold);
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
        PlayerSupernaturalData sData = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        int vampireLevel = sData.getLevel();
        int maxLevel = MysticConfig.MAX_LEVEL.get();
        float maxHealth = player.getMaxHealth();

        if (vampireLevel <= 1 || maxLevel <= 1) {
            return maxHealth * 10f;
        }

        float t = (float)(vampireLevel - 1) / (maxLevel - 1);
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
        if (!VampireService.isVampire(player)) return;
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
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ChannelService.clearOnDisconnect(player);
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
