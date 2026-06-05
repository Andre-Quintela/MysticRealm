package com.nashgoldd.mysticrealm.supernatural.vampire.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Dados vampíricos do jogador armazenados como Data Attachment.
 *
 * O sangue é derivado diretamente de {@code player.getFoodData()} — FoodData é a
 * única fonte de verdade para o recurso de sangue vampírico. Mapeamento: foodLevel
 * 0-20 → blood 0-100 (multiplicar por 5).
 */
public class VampireData {

    public static final MapCodec<VampireData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.BOOL.fieldOf("transformed").forGetter(d -> d.transformed),
            Codec.BOOL.fieldOf("sunlightBurning").forGetter(d -> d.sunlightBurning),
            Codec.BOOL.fieldOf("nearDeath").forGetter(d -> d.nearDeath)
        ).apply(instance, VampireData::new)
    );

    private boolean transformed;
    private boolean sunlightBurning;
    private boolean nearDeath;

    public VampireData() {
        this.transformed = false;
        this.sunlightBurning = false;
        this.nearDeath = false;
    }

    private VampireData(boolean transformed, boolean sunlightBurning, boolean nearDeath) {
        this.transformed = transformed;
        this.sunlightBurning = sunlightBurning;
        this.nearDeath = nearDeath;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public boolean isTransformed() {
        return transformed;
    }

    public boolean isSunlightBurning() {
        return sunlightBurning;
    }

    public boolean isNearDeath() {
        return nearDeath;
    }

    // ── Setters (servidor) ────────────────────────────────────────────────────

    public void setTransformed(boolean value) {
        this.transformed = value;
    }

    public void setSunlightBurning(boolean value) {
        this.sunlightBurning = value;
    }

    public void setNearDeath(boolean value) {
        this.nearDeath = value;
    }

    // ── Raw setters (cliente — sem disparar eventos) ──────────────────────────

    public void setTransformedRaw(boolean value) {
        this.transformed = value;
    }

    public void setSunlightBurningRaw(boolean value) {
        this.sunlightBurning = value;
    }

    public void setNearDeathRaw(boolean value) {
        this.nearDeath = value;
    }
}
