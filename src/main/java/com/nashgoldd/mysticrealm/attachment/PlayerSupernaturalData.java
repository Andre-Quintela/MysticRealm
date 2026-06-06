package com.nashgoldd.mysticrealm.attachment;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nashgoldd.mysticrealm.event.RaceChangedEvent;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

public class PlayerSupernaturalData {

    public static final MapCodec<PlayerSupernaturalData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RaceType.CODEC.fieldOf("race").forGetter(PlayerSupernaturalData::getRace)
        ).apply(instance, PlayerSupernaturalData::new)
    );

    private RaceType race;

    public PlayerSupernaturalData() {
        this(RaceType.HUMAN);
    }

    public PlayerSupernaturalData(RaceType race) {
        this.race = race;
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public RaceType getRace() {
        return race;
    }

    // ── Setters (servidor) — disparam eventos ───────────────────────────────

    public void setRace(RaceType newRace, Player player) {
        RaceType old = this.race;
        this.race = newRace;
        MysticRealmLogger.debug("Raça alterada: {} → {} (jogador: {})", old, newRace, player.getName().getString());
        NeoForge.EVENT_BUS.post(new RaceChangedEvent(player, old, newRace));
    }

    // ── Setters raw (cliente) — apenas sincronização local sem eventos ──────

    public void setRaceRaw(RaceType race) {
        this.race = race;
    }
}
