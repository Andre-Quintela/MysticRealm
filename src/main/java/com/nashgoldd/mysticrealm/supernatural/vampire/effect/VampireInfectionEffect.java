package com.nashgoldd.mysticrealm.supernatural.vampire.effect;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticEffects;
import com.nashgoldd.mysticrealm.supernatural.transformation.IPendingTransformation;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.PlayerVampireTransformationEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireInfectionExpireEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.common.NeoForge;

public class VampireInfectionEffect extends MobEffect implements IPendingTransformation {

    public VampireInfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void applyTransformation(ServerPlayer player, DamageSource deathCause) {
        PlayerVampireTransformationEvent transformEvent =
            new PlayerVampireTransformationEvent(player, deathCause);
        NeoForge.EVENT_BUS.post(transformEvent);

        if (transformEvent.isCanceled()) return;

        VampireService.transform(player);
        player.removeEffect(MysticEffects.VAMPIRE_INFECTION);
        player.setHealth((float) MysticConfig.VAMPIRE_MINIMUM_HEALTH.get().doubleValue());
        player.sendSystemMessage(Component.translatable("mysticrealm.vampire.transformation.complete"));

        MysticNetwork.syncToClient(player);
        MysticNetwork.syncVampireToClient(player);
        MysticNetwork.syncVampireProgressionToClient(player);

        MysticRealmLogger.debug("Jogador {} transformado em vampiro via infecção", player.getName().getString());
    }

    @Override
    public void onExpire(ServerPlayer player) {
        NeoForge.EVENT_BUS.post(new VampireInfectionExpireEvent(player));
        MysticRealmLogger.debug("Infecção vampírica expirou para {}", player.getName().getString());
    }

}
