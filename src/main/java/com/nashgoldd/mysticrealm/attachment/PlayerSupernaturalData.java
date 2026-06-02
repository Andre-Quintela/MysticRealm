package com.nashgoldd.mysticrealm.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.event.ExperienceChangedEvent;
import com.nashgoldd.mysticrealm.event.LevelChangedEvent;
import com.nashgoldd.mysticrealm.event.RaceChangedEvent;
import com.nashgoldd.mysticrealm.supernatural.race.RaceType;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dados sobrenaturais persistentes de cada jogador.
 *
 * Persistência garantida por:
 *   - CODEC (MapCodec): serializa/desserializa do NBT do jogador (login/logout, dimensões)
 *   - copyOnDeath(): copiado automaticamente ao respawnar (declarado em MysticAttachments)
 *
 * Os setters com Player disparam eventos no NeoForge.EVENT_BUS para permitir
 * que outros sistemas reajam a mudanças sem acoplar código aqui.
 * Os setters "raw" são para uso no cliente, onde não queremos disparar eventos.
 *
 * Nota: AttachmentType.Builder.serialize() exige MapCodec (não Codec), por isso
 * usamos RecordCodecBuilder.mapCodec() em vez de RecordCodecBuilder.create().
 */
public class PlayerSupernaturalData {

    public static final MapCodec<PlayerSupernaturalData> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            RaceType.CODEC.fieldOf("race").forGetter(PlayerSupernaturalData::getRace),
            Codec.INT.fieldOf("level").forGetter(PlayerSupernaturalData::getLevel),
            Codec.LONG.fieldOf("experience").forGetter(PlayerSupernaturalData::getExperience)
        ).apply(instance, PlayerSupernaturalData::new)
    );

    private RaceType race;
    private int level;
    private long experience;

    // Construtor padrão para novos jogadores (sem dados salvos)
    public PlayerSupernaturalData() {
        this(RaceType.HUMAN, 1, 0L);
    }

    // Construtor usado pelo CODEC na desserialização
    public PlayerSupernaturalData(RaceType race, int level, long experience) {
        this.race = race;
        this.level = Math.max(1, level);
        this.experience = Math.max(0L, experience);
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public RaceType getRace() {
        return race;
    }

    public int getLevel() {
        return level;
    }

    public long getExperience() {
        return experience;
    }

    // ── Setters (servidor) — disparam eventos ───────────────────────────────

    public void setRace(RaceType newRace, Player player) {
        RaceType old = this.race;
        this.race = newRace;
        MysticRealmLogger.debug("Raça alterada: {} → {} (jogador: {})", old, newRace, player.getName().getString());
        NeoForge.EVENT_BUS.post(new RaceChangedEvent(player, old, newRace));
    }

    public void setLevel(int newLevel, Player player) {
        int clamped = Math.max(1, Math.min(newLevel, MysticConfig.MAX_LEVEL.get()));
        int old = this.level;
        this.level = clamped;
        MysticRealmLogger.debug("Level alterado: {} → {} (jogador: {})", old, clamped, player.getName().getString());
        NeoForge.EVENT_BUS.post(new LevelChangedEvent(player, old, clamped));
    }

    public void setExperience(long xp, Player player) {
        long clamped = Math.max(0L, xp);
        long old = this.experience;
        this.experience = clamped;
        MysticRealmLogger.debug("XP alterado: {} → {} (jogador: {})", old, clamped, player.getName().getString());
        NeoForge.EVENT_BUS.post(new ExperienceChangedEvent(player, old, clamped));
    }

    public void addExperience(long amount, Player player) {
        setExperience(this.experience + amount, player);
    }

    // ── Setters raw (cliente) — apenas sincronização local sem eventos ──────

    public void setRaceRaw(RaceType race) {
        this.race = race;
    }

    public void setLevelRaw(int level) {
        this.level = Math.max(1, level);
    }

    public void setExperienceRaw(long experience) {
        this.experience = Math.max(0L, experience);
    }
}
