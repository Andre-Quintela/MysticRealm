package com.nashgoldd.mysticrealm.supernatural.vampire.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nashgoldd.mysticrealm.supernatural.resource.RaceResource;
import com.nashgoldd.mysticrealm.supernatural.resource.ResourceType;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.BloodLevelChangedEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dados vampíricos do jogador armazenados como Data Attachment.
 *
 * O sangue é modelado como {@link RaceResource} (sistema genérico reutilizável por
 * lobisomens e bruxaria), nunca como um campo int vampírico direto.
 */
public class VampireData {

    public static final MapCodec<VampireData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RaceResource.CODEC.fieldOf("blood").forGetter(d -> d.blood),
            Codec.BOOL.fieldOf("transformed").forGetter(d -> d.transformed),
            Codec.BOOL.fieldOf("sunlightBurning").forGetter(d -> d.sunlightBurning),
            Codec.BOOL.fieldOf("nearDeath").forGetter(d -> d.nearDeath)
        ).apply(instance, VampireData::new)
    );

    private RaceResource blood;
    private boolean transformed;
    private boolean sunlightBurning;
    private boolean nearDeath;

    public VampireData() {
        this(new RaceResource(ResourceType.BLOOD, 100, 100), false, false, false);
    }

    public VampireData(RaceResource blood, boolean transformed, boolean sunlightBurning, boolean nearDeath) {
        this.blood = blood;
        this.transformed = transformed;
        this.sunlightBurning = sunlightBurning;
        this.nearDeath = nearDeath;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int getBloodLevel() {
        return blood.getCurrent();
    }

    public int getMaxBlood() {
        return blood.getMax();
    }

    public boolean isTransformed() {
        return transformed;
    }

    public boolean isSunlightBurning() {
        return sunlightBurning;
    }

    public boolean isNearDeath() {
        return nearDeath;
    }

    // ── Setters com eventos (servidor) ────────────────────────────────────────

    public void setBloodLevel(int value, Player player) {
        int old = blood.getCurrent();
        blood.setCurrentRaw(value);
        int newVal = blood.getCurrent();
        if (old != newVal) {
            NeoForge.EVENT_BUS.post(new BloodLevelChangedEvent(player, old, newVal));
        }
    }

    public void addBlood(int amount, Player player) {
        setBloodLevel(blood.getCurrent() + amount, player);
    }

    public void removeBlood(int amount, Player player) {
        setBloodLevel(blood.getCurrent() - amount, player);
    }

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

    public void setBloodLevelRaw(int value) {
        blood.setCurrentRaw(value);
    }

    public void setMaxBloodRaw(int max) {
        this.blood = new RaceResource(blood.getType(), blood.getCurrent(), max);
    }

    public void setTransformedRaw(boolean value) {
        this.transformed = value;
    }

    public void setSunlightBurningRaw(boolean value) {
        this.sunlightBurning = value;
    }

    public void setNearDeathRaw(boolean value) {
        this.nearDeath = value;
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    public void resetToDefaults(int startingBlood, int maxBlood) {
        this.blood = new RaceResource(ResourceType.BLOOD, startingBlood, maxBlood);
        this.transformed = true;
        this.sunlightBurning = false;
        this.nearDeath = false;
    }
}
