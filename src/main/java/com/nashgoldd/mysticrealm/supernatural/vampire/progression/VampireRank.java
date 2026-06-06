package com.nashgoldd.mysticrealm.supernatural.vampire.progression;

import java.util.Optional;

public enum VampireRank {
    NEWBORN,
    NEOPHYTE,
    VAMPIRE,
    ELDER,
    VAMPIRE_LORD,
    PRINCE_OF_NIGHT,
    BLOOD_SOVEREIGN;

    private static final VampireRank[] VALUES = values();

    public Optional<VampireRank> next() {
        int idx = ordinal() + 1;
        return idx < VALUES.length ? Optional.of(VALUES[idx]) : Optional.empty();
    }

    public boolean isMax() {
        return this == BLOOD_SOVEREIGN;
    }

    public String displayName() {
        return switch (this) {
            case NEWBORN -> "Newborn";
            case NEOPHYTE -> "Neophyte";
            case VAMPIRE -> "Vampire";
            case ELDER -> "Elder";
            case VAMPIRE_LORD -> "Vampire Lord";
            case PRINCE_OF_NIGHT -> "Prince of Night";
            case BLOOD_SOVEREIGN -> "Blood Sovereign";
        };
    }
}
