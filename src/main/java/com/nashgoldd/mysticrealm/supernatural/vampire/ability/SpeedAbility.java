package com.nashgoldd.mysticrealm.supernatural.vampire.ability;

import com.nashgoldd.mysticrealm.supernatural.ability.IAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class SpeedAbility implements IAbility {

    public static final String ID = "speed";

    @Override public String getId() { return ID; }
    @Override public String getDisplayName() { return "Speed"; }
    @Override public int getIconColor() { return 0xFF00FF88; }

    @Override
    public void activate(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 99999, 0, true, false));
    }

    @Override
    public void deactivate(ServerPlayer player) {
        player.removeEffect(MobEffects.SPEED);
    }
}
