package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registro central de {@link IMultiblockStructure}s, no mesmo padrão de AbilityRegistry.
 */
public final class StructureRegistry {

    private static final Map<Identifier, IMultiblockStructure> REGISTRY = new LinkedHashMap<>();

    private StructureRegistry() {}

    public static void register(IMultiblockStructure structure) {
        REGISTRY.put(structure.id(), structure);
    }

    public static Optional<IMultiblockStructure> get(Identifier id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Collection<IMultiblockStructure> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
