package com.nashgoldd.mysticrealm.supernatural.race;

/**
 * Contrato mínimo para qualquer raça sobrenatural.
 * Ponto de extensão para implementações futuras (VampireRace, WerewolfRace, etc.).
 */
public interface IRace {
    RaceType getType();
}
