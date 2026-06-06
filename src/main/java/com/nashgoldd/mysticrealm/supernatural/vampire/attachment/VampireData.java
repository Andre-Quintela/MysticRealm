package com.nashgoldd.mysticrealm.supernatural.vampire.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nashgoldd.mysticrealm.supernatural.vampire.progression.VampireRank;

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
            Codec.BOOL.fieldOf("nearDeath").forGetter(d -> d.nearDeath),
            Codec.STRING.xmap(VampireRank::valueOf, VampireRank::name)
                .optionalFieldOf("rank", VampireRank.NEWBORN).forGetter(d -> d.rank),
            Codec.LONG.optionalFieldOf("bloodEssence", 0L).forGetter(d -> d.bloodEssence),
            Codec.LONG.optionalFieldOf("vampireAgeTicks", 0L).forGetter(d -> d.vampireAgeTicks),
            Codec.INT.optionalFieldOf("ascensionCount", 0).forGetter(d -> d.ascensionCount)
        ).apply(instance, VampireData::new)
    );

    private boolean transformed;
    private boolean sunlightBurning;
    private boolean nearDeath;
    private VampireRank rank;
    private long bloodEssence;
    private long vampireAgeTicks;
    private int ascensionCount;

    public VampireData() {
        this.transformed = false;
        this.sunlightBurning = false;
        this.nearDeath = false;
        this.rank = VampireRank.NEWBORN;
        this.bloodEssence = 0L;
        this.vampireAgeTicks = 0L;
        this.ascensionCount = 0;
    }

    private VampireData(boolean transformed, boolean sunlightBurning, boolean nearDeath,
                        VampireRank rank, long bloodEssence, long vampireAgeTicks, int ascensionCount) {
        this.transformed = transformed;
        this.sunlightBurning = sunlightBurning;
        this.nearDeath = nearDeath;
        this.rank = rank;
        this.bloodEssence = bloodEssence;
        this.vampireAgeTicks = vampireAgeTicks;
        this.ascensionCount = ascensionCount;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public boolean isTransformed() { return transformed; }
    public boolean isSunlightBurning() { return sunlightBurning; }
    public boolean isNearDeath() { return nearDeath; }
    public VampireRank getRank() { return rank; }
    public long getBloodEssence() { return bloodEssence; }
    public long getVampireAgeTicks() { return vampireAgeTicks; }
    public int getAscensionCount() { return ascensionCount; }

    // ── Setters (servidor) ────────────────────────────────────────────────────

    public void setTransformed(boolean value) { this.transformed = value; }
    public void setSunlightBurning(boolean value) { this.sunlightBurning = value; }
    public void setNearDeath(boolean value) { this.nearDeath = value; }
    public void setRank(VampireRank value) { this.rank = value; }
    public void setBloodEssence(long value) { this.bloodEssence = Math.max(0L, value); }
    public void addBloodEssence(long amount) { this.bloodEssence = Math.max(0L, this.bloodEssence + amount); }
    public void setVampireAgeTicks(long value) { this.vampireAgeTicks = Math.max(0L, value); }
    public void incrementVampireAgeTicks() { this.vampireAgeTicks++; }
    public void setAscensionCount(int value) { this.ascensionCount = Math.max(0, value); }
    public void incrementAscensionCount() { this.ascensionCount++; }

    // ── Raw setters (cliente — sem disparar eventos) ──────────────────────────

    public void setTransformedRaw(boolean value) { this.transformed = value; }
    public void setSunlightBurningRaw(boolean value) { this.sunlightBurning = value; }
    public void setNearDeathRaw(boolean value) { this.nearDeath = value; }
    public void setRankRaw(VampireRank value) { this.rank = value; }
    public void setBloodEssenceRaw(long value) { this.bloodEssence = value; }
    public void setVampireAgeTicksRaw(long value) { this.vampireAgeTicks = value; }
    public void setAscensionCountRaw(int value) { this.ascensionCount = value; }
}
