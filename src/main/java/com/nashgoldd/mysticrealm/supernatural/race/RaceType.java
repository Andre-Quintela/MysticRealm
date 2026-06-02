package com.nashgoldd.mysticrealm.supernatural.race;

import com.mojang.serialization.Codec;

/**
 * Raças sobrenaturais disponíveis no mod.
 * CODEC incluído para serialização NBT e rede sem depender de registro externo.
 */
public enum RaceType {
    HUMAN,
    VAMPIRE,
    WEREWOLF,
    WITCH;

    public static final Codec<RaceType> CODEC = Codec.STRING.xmap(
        s -> RaceType.valueOf(s.toUpperCase()),
        RaceType::name
    );
}
