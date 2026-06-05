package com.nashgoldd.mysticrealm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MysticConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── Geral ────────────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    // ── Vampiro ───────────────────────────────────────────────────────────────

    public static final ModConfigSpec.IntValue VAMPIRE_BLOOD_DRAIN_AMOUNT;
    public static final ModConfigSpec.IntValue VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS;
    public static final ModConfigSpec.BooleanValue VAMPIRE_SUNLIGHT_DAMAGE_ENABLED;
    public static final ModConfigSpec.DoubleValue VAMPIRE_SUNLIGHT_MAX_SURVIVAL_SECONDS;
    public static final ModConfigSpec.IntValue VAMPIRE_REGENERATION_THRESHOLD;
    public static final ModConfigSpec.IntValue VAMPIRE_SPEED_THRESHOLD;
    public static final ModConfigSpec.BooleanValue VAMPIRE_IMMORTALITY_ENABLED;
    public static final ModConfigSpec.DoubleValue VAMPIRE_MINIMUM_HEALTH;
    public static final ModConfigSpec.IntValue VAMPIRE_NEAR_DEATH_DEBUFF_DURATION;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Configurações gerais do MysticRealm").push("general");

        MAX_LEVEL = BUILDER
            .comment("Nível máximo que um jogador sobrenatural pode atingir")
            .defineInRange("maxLevel", 100, 1, Integer.MAX_VALUE);

        DEBUG_LOGGING = BUILDER
            .comment("Ativar mensagens de debug no console")
            .define("debugLogging", true);

        BUILDER.pop();

        BUILDER.comment("Configurações do sistema vampírico").push("vampire");

        VAMPIRE_BLOOD_DRAIN_AMOUNT = BUILDER
            .comment("Quantidade de sangue drenado por intervalo")
            .defineInRange("bloodDrainAmount", 1, 0, Integer.MAX_VALUE);

        VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS = BUILDER
            .comment("Intervalo de drenagem passiva de sangue em segundos")
            .defineInRange("bloodDrainIntervalSeconds", 60, 1, Integer.MAX_VALUE);

        VAMPIRE_SUNLIGHT_DAMAGE_ENABLED = BUILDER
            .comment("Habilitar dano de luz solar para vampiros")
            .define("sunlightDamageEnabled", true);

        VAMPIRE_SUNLIGHT_MAX_SURVIVAL_SECONDS = BUILDER
            .comment("Tempo máximo de sobrevivência ao sol no nível máximo (segundos). Level 1 sempre resulta em morte instantânea.")
            .defineInRange("sunlightMaxSurvivalSeconds", 10.0, 1.0, Double.MAX_VALUE);

        VAMPIRE_REGENERATION_THRESHOLD = BUILDER
            .comment("Nível de sangue mínimo para regeneração passiva (0-100)")
            .defineInRange("regenerationThreshold", 75, 0, 100);

        VAMPIRE_SPEED_THRESHOLD = BUILDER
            .comment("Nível de sangue mínimo para velocidade passiva (0-100)")
            .defineInRange("speedThreshold", 50, 0, 100);

        VAMPIRE_IMMORTALITY_ENABLED = BUILDER
            .comment("Habilitar imortalidade vampírica (apenas fraquezas sobrenaturais podem matar)")
            .define("immortalityEnabled", true);

        VAMPIRE_MINIMUM_HEALTH = BUILDER
            .comment("HP mínimo ao qual a imortalidade reduz o vampiro (meio coração = 1.0)")
            .defineInRange("minimumHealth", 1.0, 0.5, 20.0);

        VAMPIRE_NEAR_DEATH_DEBUFF_DURATION = BUILDER
            .comment("Duração dos debuffs de quase-morte em ticks (600 = 30 segundos)")
            .defineInRange("nearDeathDebuffDuration", 600, 20, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private MysticConfig() {}
}
