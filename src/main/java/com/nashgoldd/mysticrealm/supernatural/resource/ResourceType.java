package com.nashgoldd.mysticrealm.supernatural.resource;

import com.mojang.serialization.Codec;

public enum ResourceType {
    BLOOD,
    RAGE,
    MANA;

    public static final Codec<ResourceType> CODEC =
        Codec.STRING.xmap(s -> ResourceType.valueOf(s.toUpperCase()), ResourceType::name);
}
