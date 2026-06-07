package com.nashgoldd.mysticrealm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MysticConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ── Geral ────────────────────────────────────────────────────────────────

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

    // ── Pool de sangue das entidades ──────────────────────────────────────────

    public static final ModConfigSpec.DoubleValue ENTITY_BLOOD_REGEN_FRACTION;
    public static final ModConfigSpec.IntValue    ENTITY_BLOOD_REGEN_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue ENTITY_EXSANGUINATION_DAMAGE;
    public static final ModConfigSpec.DoubleValue BLOOD_DRAIN_AMOUNT_PER_INTERVAL;

    // ── Progressão vampírica ──────────────────────────────────────────────────

    // ── Spawn do vampiro hostil ───────────────────────────────────────────────

    public static final ModConfigSpec.BooleanValue ENABLE_VAMPIRE_SPAWN;
    public static final ModConfigSpec.IntValue     VAMPIRE_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue     VAMPIRE_SPAWN_MIN_GROUP;
    public static final ModConfigSpec.IntValue     VAMPIRE_SPAWN_MAX_GROUP;

    // ── Progressão vampírica ──────────────────────────────────────────────────

    public static final ModConfigSpec.BooleanValue ENABLE_VAMPIRE_PROGRESSION;
    public static final ModConfigSpec.BooleanValue TRACK_VAMPIRE_AGE;

    public static final ModConfigSpec.LongValue NEWBORN_TO_NEOPHYTE_ESSENCE;
    public static final ModConfigSpec.LongValue NEOPHYTE_TO_VAMPIRE_ESSENCE;
    public static final ModConfigSpec.LongValue VAMPIRE_TO_ELDER_ESSENCE;
    public static final ModConfigSpec.LongValue ELDER_TO_LORD_ESSENCE;
    public static final ModConfigSpec.LongValue LORD_TO_PRINCE_ESSENCE;
    public static final ModConfigSpec.LongValue PRINCE_TO_SOVEREIGN_ESSENCE;

    public static final ModConfigSpec.LongValue NEWBORN_TO_NEOPHYTE_AGE_HOURS;
    public static final ModConfigSpec.LongValue NEOPHYTE_TO_VAMPIRE_AGE_HOURS;
    public static final ModConfigSpec.LongValue VAMPIRE_TO_ELDER_AGE_HOURS;
    public static final ModConfigSpec.LongValue ELDER_TO_LORD_AGE_HOURS;
    public static final ModConfigSpec.LongValue LORD_TO_PRINCE_AGE_HOURS;
    public static final ModConfigSpec.LongValue PRINCE_TO_SOVEREIGN_AGE_HOURS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("Configurações gerais do MysticRealm").push("general");

        DEBUG_LOGGING = BUILDER
            .translation("mysticrealm.configuration.general.debugLogging")
            .define("debugLogging", true);

        BUILDER.pop();

        BUILDER.comment("Configurações do sistema vampírico").push("vampire");

        VAMPIRE_BLOOD_DRAIN_AMOUNT = BUILDER
            .translation("mysticrealm.configuration.vampire.bloodDrainAmount")
            .defineInRange("bloodDrainAmount", 1, 0, Integer.MAX_VALUE);

        VAMPIRE_BLOOD_DRAIN_INTERVAL_SECONDS = BUILDER
            .translation("mysticrealm.configuration.vampire.bloodDrainIntervalSeconds")
            .defineInRange("bloodDrainIntervalSeconds", 60, 1, Integer.MAX_VALUE);

        VAMPIRE_SUNLIGHT_DAMAGE_ENABLED = BUILDER
            .translation("mysticrealm.configuration.vampire.sunlightDamageEnabled")
            .define("sunlightDamageEnabled", true);

        VAMPIRE_SUNLIGHT_MAX_SURVIVAL_SECONDS = BUILDER
            .translation("mysticrealm.configuration.vampire.sunlightMaxSurvivalSeconds")
            .defineInRange("sunlightMaxSurvivalSeconds", 10.0, 1.0, Double.MAX_VALUE);

        VAMPIRE_REGENERATION_THRESHOLD = BUILDER
            .translation("mysticrealm.configuration.vampire.regenerationThreshold")
            .defineInRange("regenerationThreshold", 75, 0, 100);

        VAMPIRE_SPEED_THRESHOLD = BUILDER
            .translation("mysticrealm.configuration.vampire.speedThreshold")
            .defineInRange("speedThreshold", 50, 0, 100);

        VAMPIRE_IMMORTALITY_ENABLED = BUILDER
            .translation("mysticrealm.configuration.vampire.immortalityEnabled")
            .define("immortalityEnabled", true);

        VAMPIRE_MINIMUM_HEALTH = BUILDER
            .translation("mysticrealm.configuration.vampire.minimumHealth")
            .defineInRange("minimumHealth", 1.0, 0.5, 20.0);

        VAMPIRE_NEAR_DEATH_DEBUFF_DURATION = BUILDER
            .translation("mysticrealm.configuration.vampire.nearDeathDebuffDuration")
            .defineInRange("nearDeathDebuffDuration", 600, 20, Integer.MAX_VALUE);

        ENTITY_BLOOD_REGEN_FRACTION = BUILDER
            .translation("mysticrealm.configuration.vampire.entityBloodRegenFraction")
            .defineInRange("entityBloodRegenFraction", 0.05, 0.0, 1.0);

        ENTITY_BLOOD_REGEN_INTERVAL_TICKS = BUILDER
            .translation("mysticrealm.configuration.vampire.entityBloodRegenIntervalTicks")
            .defineInRange("entityBloodRegenIntervalTicks", 1200, 1, Integer.MAX_VALUE);

        ENTITY_EXSANGUINATION_DAMAGE = BUILDER
            .translation("mysticrealm.configuration.vampire.exsanguinationDamage")
            .defineInRange("exsanguinationDamage", 1.0, 0.0, Double.MAX_VALUE);

        BLOOD_DRAIN_AMOUNT_PER_INTERVAL = BUILDER
            .translation("mysticrealm.configuration.vampire.bloodDrainAmountPerInterval")
            .defineInRange("bloodDrainAmountPerInterval", 0.5, 0.0, Double.MAX_VALUE);

        BUILDER.comment("Progressão vampírica (rank, essência, idade)").push("progression");

        ENABLE_VAMPIRE_PROGRESSION = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.enableVampireProgression")
            .define("enableVampireProgression", true);

        TRACK_VAMPIRE_AGE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.trackVampireAge")
            .define("trackVampireAge", true);

        NEWBORN_TO_NEOPHYTE_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.newbornToNeophyteEssence")
            .defineInRange("newbornToNeophyteEssence", 100L, 1L, Long.MAX_VALUE);

        NEOPHYTE_TO_VAMPIRE_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.neophyteToVampireEssence")
            .defineInRange("neophyteToVampireEssence", 500L, 1L, Long.MAX_VALUE);

        VAMPIRE_TO_ELDER_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.vampireToElderEssence")
            .defineInRange("vampireToElderEssence", 2000L, 1L, Long.MAX_VALUE);

        ELDER_TO_LORD_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.elderToLordEssence")
            .defineInRange("elderToLordEssence", 10000L, 1L, Long.MAX_VALUE);

        LORD_TO_PRINCE_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.lordToPrinceEssence")
            .defineInRange("lordToPrinceEssence", 50000L, 1L, Long.MAX_VALUE);

        PRINCE_TO_SOVEREIGN_ESSENCE = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.princeToSovereignEssence")
            .defineInRange("princeToSovereignEssence", 250000L, 1L, Long.MAX_VALUE);

        NEWBORN_TO_NEOPHYTE_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.newbornToNeophyteAgeHours")
            .defineInRange("newbornToNeophyteAgeHours", 1L, 0L, Long.MAX_VALUE);

        NEOPHYTE_TO_VAMPIRE_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.neophyteToVampireAgeHours")
            .defineInRange("neophyteToVampireAgeHours", 5L, 0L, Long.MAX_VALUE);

        VAMPIRE_TO_ELDER_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.vampireToElderAgeHours")
            .defineInRange("vampireToElderAgeHours", 15L, 0L, Long.MAX_VALUE);

        ELDER_TO_LORD_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.elderToLordAgeHours")
            .defineInRange("elderToLordAgeHours", 50L, 0L, Long.MAX_VALUE);

        LORD_TO_PRINCE_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.lordToPrinceAgeHours")
            .defineInRange("lordToPrinceAgeHours", 100L, 0L, Long.MAX_VALUE);

        PRINCE_TO_SOVEREIGN_AGE_HOURS = BUILDER
            .translation("mysticrealm.configuration.vampire.progression.princeToSovereignAgeHours")
            .defineInRange("princeToSovereignAgeHours", 250L, 0L, Long.MAX_VALUE);

        BUILDER.pop(); // progression

        BUILDER.comment("Configurações de spawn do vampiro hostil").push("spawn");

        ENABLE_VAMPIRE_SPAWN = BUILDER
            .translation("mysticrealm.configuration.vampire.spawn.enableVampireSpawn")
            .define("enableVampireSpawn", true);

        VAMPIRE_SPAWN_WEIGHT = BUILDER
            .translation("mysticrealm.configuration.vampire.spawn.vampireSpawnWeight")
            .defineInRange("vampireSpawnWeight", 5, 1, 100);

        VAMPIRE_SPAWN_MIN_GROUP = BUILDER
            .translation("mysticrealm.configuration.vampire.spawn.vampireSpawnMinGroup")
            .defineInRange("vampireSpawnMinGroup", 1, 1, 8);

        VAMPIRE_SPAWN_MAX_GROUP = BUILDER
            .translation("mysticrealm.configuration.vampire.spawn.vampireSpawnMaxGroup")
            .defineInRange("vampireSpawnMaxGroup", 2, 1, 8);

        BUILDER.pop(); // spawn

        BUILDER.pop(); // vampire

        SPEC = BUILDER.build();
    }

    private MysticConfig() {}
}
