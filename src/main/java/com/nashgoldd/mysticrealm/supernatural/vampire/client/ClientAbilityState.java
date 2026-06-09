package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ClientAbilityState {

    public static Map<Integer, String> slots          = new HashMap<>();
    public static Set<String>          activeAbilities = new HashSet<>();

    private ClientAbilityState() {}

    public static void reset() {
        slots          = new HashMap<>();
        activeAbilities = new HashSet<>();
    }

    public static Map<Integer, String> getSlots() {
        return Collections.unmodifiableMap(slots);
    }

    public static Set<String> getActiveAbilities() {
        return Collections.unmodifiableSet(activeAbilities);
    }
}
