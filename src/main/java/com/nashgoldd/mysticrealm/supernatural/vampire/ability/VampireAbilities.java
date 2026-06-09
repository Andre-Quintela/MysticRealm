package com.nashgoldd.mysticrealm.supernatural.vampire.ability;

import com.nashgoldd.mysticrealm.supernatural.ability.AbilityRegistry;

public final class VampireAbilities {

    public static final NightVisionAbility     NIGHT_VISION      = new NightVisionAbility();
    public static final SpeedAbility           SPEED             = new SpeedAbility();
    public static final StrengthAbility        STRENGTH          = new StrengthAbility();
    public static final SupernaturalSightAbility SUPERNATURAL_SIGHT = new SupernaturalSightAbility();

    private VampireAbilities() {}

    public static void register() {
        AbilityRegistry.register(NIGHT_VISION);
        AbilityRegistry.register(SPEED);
        AbilityRegistry.register(STRENGTH);
        AbilityRegistry.register(SUPERNATURAL_SIGHT);
    }
}
