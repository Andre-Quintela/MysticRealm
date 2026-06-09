package com.nashgoldd.mysticrealm.supernatural.ability;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class AbilityRegistry {

    private static final Map<String, IAbility> REGISTRY = new LinkedHashMap<>();

    private AbilityRegistry() {}

    public static void register(IAbility ability) {
        REGISTRY.put(ability.getId(), ability);
    }

    public static Optional<IAbility> get(String id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Collection<String> getIds() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }
}
