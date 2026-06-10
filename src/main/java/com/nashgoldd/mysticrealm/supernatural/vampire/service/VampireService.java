package com.nashgoldd.mysticrealm.supernatural.vampire.service;

import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireCuredEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireTransformEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

public final class VampireService {

    private VampireService() {}

    public static boolean isVampire(Player player) {
        return player.getData(MysticAttachments.SUPERNATURAL_DATA).getRace() == RaceType.VAMPIRE;
    }

    public static VampireData getData(Player player) {
        return player.getData(MysticAttachments.VAMPIRE_DATA);
    }

    public static void transform(Player player) {
        PlayerSupernaturalData sData = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        VampireData vData = player.getData(MysticAttachments.VAMPIRE_DATA);

        RaceType oldRace = sData.getRace();

        sData.setRace(RaceType.VAMPIRE, player);

        vData.setTransformed(true);
        vData.setSunlightBurning(false);
        vData.setNearDeath(false);
        // Sangue cheio ao transformar (foodLevel 20 = 100% sangue)
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5f);

        NeoForge.EVENT_BUS.post(new VampireTransformEvent(player, oldRace));

        if (player instanceof ServerPlayer sp) {
            MysticNetwork.syncToClient(sp);
            MysticNetwork.syncVampireToClient(sp);
        }
    }

    public static void cure(Player player) {
        PlayerSupernaturalData sData = player.getData(MysticAttachments.SUPERNATURAL_DATA);
        VampireData vData = player.getData(MysticAttachments.VAMPIRE_DATA);

        sData.setRace(RaceType.HUMAN, player);
        vData.setTransformed(false);
        vData.setNearDeath(false);
        vData.setSunlightBurning(false);
        // Restaurar fome vanilla ao curar
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(5f);

        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.SPEED);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.SLOWNESS);

        NeoForge.EVENT_BUS.post(new VampireCuredEvent(player));

        if (player instanceof ServerPlayer sp) {
            MysticNetwork.syncToClient(sp);
            MysticNetwork.syncVampireToClient(sp);
        }
    }
}
