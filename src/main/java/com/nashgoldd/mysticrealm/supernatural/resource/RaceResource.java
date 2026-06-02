package com.nashgoldd.mysticrealm.supernatural.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Recurso sobrenatural genérico (sangue para vampiros, raiva para lobisomens, mana para bruxas).
 * Reutilizável por todas as raças futuras — nunca acoplado a mecânicas vampíricas.
 */
public class RaceResource {

    public static final MapCodec<RaceResource> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceType.CODEC.fieldOf("type").forGetter(RaceResource::getType),
            Codec.INT.fieldOf("current").forGetter(RaceResource::getCurrent),
            Codec.INT.fieldOf("max").forGetter(RaceResource::getMax)
        ).apply(instance, RaceResource::new)
    );

    private final ResourceType type;
    private int current;
    private final int max;

    public RaceResource(ResourceType type, int current, int max) {
        this.type = type;
        this.max = Math.max(1, max);
        this.current = Math.max(0, Math.min(current, this.max));
    }

    public ResourceType getType() {
        return type;
    }

    public int getCurrent() {
        return current;
    }

    public int getMax() {
        return max;
    }

    public float getPercent() {
        return (float) current / max;
    }

    public boolean isEmpty() {
        return current <= 0;
    }

    public boolean isFull() {
        return current >= max;
    }

    public void add(int amount) {
        current = Math.min(current + amount, max);
    }

    public void remove(int amount) {
        current = Math.max(current - amount, 0);
    }

    public void setCurrentRaw(int value) {
        current = Math.max(0, Math.min(value, max));
    }

    public RaceResource copy() {
        return new RaceResource(type, current, max);
    }
}
