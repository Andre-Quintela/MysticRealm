package com.nashgoldd.mysticrealm.supernatural.vampire.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.supernatural.vampire.balance.BloodBalance;
import net.minecraft.world.entity.LivingEntity;

public class EntityBloodData {

    public static final MapCodec<EntityBloodData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("currentBlood").forGetter(d -> d.currentBlood),
            Codec.FLOAT.fieldOf("maxBlood").forGetter(d -> d.maxBlood),
            Codec.BOOL.fieldOf("initialized").forGetter(d -> d.initialized)
        ).apply(instance, EntityBloodData::new)
    );

    private float currentBlood;
    private float maxBlood;
    private boolean initialized;

    public EntityBloodData() {
        this.currentBlood = 0f;
        this.maxBlood = 0f;
        this.initialized = false;
    }

    private EntityBloodData(float currentBlood, float maxBlood, boolean initialized) {
        this.currentBlood = currentBlood;
        this.maxBlood = maxBlood;
        this.initialized = initialized;
    }

    public static EntityBloodData getOrInit(LivingEntity entity) {
        EntityBloodData data = entity.getData(MysticAttachments.ENTITY_BLOOD);
        if (!data.initialized) {
            data.maxBlood = BloodBalance.maxBloodForEntity(entity);
            data.currentBlood = data.maxBlood;
            data.initialized = true;
            entity.setData(MysticAttachments.ENTITY_BLOOD, data);
        }
        return data;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public float getCurrentBlood() { return currentBlood; }
    public float getMaxBlood()     { return maxBlood; }
    public boolean isInitialized() { return initialized; }
    public boolean isEmpty()       { return currentBlood <= 0f; }
    public boolean isFull()        { return currentBlood >= maxBlood; }

    // ── Mutações ─────────────────────────────────────────────────────────────

    public float drain(float amount) {
        float actual = Math.min(currentBlood, amount);
        currentBlood = Math.max(0f, currentBlood - amount);
        return actual;
    }

    public void regenerate(float amount) {
        currentBlood = Math.min(maxBlood, currentBlood + amount);
    }

    public void setCurrentBlood(float value) {
        currentBlood = Math.max(0f, Math.min(maxBlood, value));
    }
}
