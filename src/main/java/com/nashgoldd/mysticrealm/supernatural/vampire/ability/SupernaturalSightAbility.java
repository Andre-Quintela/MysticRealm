package com.nashgoldd.mysticrealm.supernatural.vampire.ability;

import com.nashgoldd.mysticrealm.supernatural.ability.IAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class SupernaturalSightAbility implements IAbility {

    public static final String ID = "supernatural_sight";

    @Override public String getId() { return ID; }
    @Override public String getDisplayName() { return "Supernatural Sight"; }
    @Override public int getIconColor() { return 0xFFFFDD00; }

    @Override
    public void activate(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 99999, 0, true, false));
    }

    @Override
    public void deactivate(ServerPlayer player) {
        player.removeEffect(MobEffects.GLOWING);
    }
}
