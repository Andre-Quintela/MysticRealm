package com.nashgoldd.mysticrealm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MysticClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── HUD ──────────────────────────────────────────────────────────────────

    public static final ModConfigSpec.DoubleValue ABILITY_WHEEL_SCALE;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Configurações de interface do cliente").push("hud");

        ABILITY_WHEEL_SCALE = BUILDER
            .translation("mysticrealm.configuration.hud.abilityWheelScale")
            .defineInRange("abilityWheelScale", 2.0, 0.5, 3.0);

        BUILDER.pop(); // hud

        SPEC = BUILDER.build();
    }

    private MysticClientConfig() {}
}
