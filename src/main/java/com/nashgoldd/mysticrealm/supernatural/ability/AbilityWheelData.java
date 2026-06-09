package com.nashgoldd.mysticrealm.supernatural.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.*;
import java.util.stream.Collectors;

public final class AbilityWheelData {

    public static final int SLOTS = 8;

    // slot 1-8 → abilityId
    private final Map<Integer, String> slots;
    // IDs atualmente ativados
    private final Set<String> activeAbilities;

    public AbilityWheelData() {
        this.slots = new HashMap<>();
        this.activeAbilities = new HashSet<>();
    }

    // Construtor usado pelo codec
    private AbilityWheelData(Map<String, String> slotsStr, List<String> activeList) {
        this.slots = stringMapToSlots(slotsStr);
        this.activeAbilities = new HashSet<>(activeList);
    }

    // ── Codec ─────────────────────────────────────────────────────────────────

    public static final MapCodec<AbilityWheelData> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                .optionalFieldOf("slots", Map.of())
                .forGetter(d -> slotsToStringMap(d.slots)),
            Codec.STRING.listOf()
                .optionalFieldOf("active_abilities", List.of())
                .forGetter(d -> new ArrayList<>(d.activeAbilities))
        ).apply(inst, AbilityWheelData::new)
    );

    // ── Slots ─────────────────────────────────────────────────────────────────

    public void setSlot(int slot, String abilityId) {
        slots.put(slot, abilityId);
    }

    public void clearSlot(int slot) {
        slots.remove(slot);
    }

    public Optional<String> getSlot(int slot) {
        return Optional.ofNullable(slots.get(slot));
    }

    public Map<Integer, String> getSlots() {
        return Collections.unmodifiableMap(slots);
    }

    // ── Estados ativos ────────────────────────────────────────────────────────

    public boolean isActive(String abilityId) {
        return activeAbilities.contains(abilityId);
    }

    public void setActive(String abilityId, boolean active) {
        if (active) activeAbilities.add(abilityId);
        else activeAbilities.remove(abilityId);
    }

    public void clearAllActive() {
        activeAbilities.clear();
    }

    public Set<String> getActiveAbilities() {
        return Collections.unmodifiableSet(activeAbilities);
    }

    // ── Helpers de conversão ──────────────────────────────────────────────────

    private static Map<String, String> slotsToStringMap(Map<Integer, String> slots) {
        return slots.entrySet().stream()
            .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
    }

    private static Map<Integer, String> stringMapToSlots(Map<String, String> map) {
        Map<Integer, String> result = new HashMap<>();
        map.forEach((k, v) -> {
            try { result.put(Integer.parseInt(k), v); } catch (NumberFormatException ignored) {}
        });
        return result;
    }
}
