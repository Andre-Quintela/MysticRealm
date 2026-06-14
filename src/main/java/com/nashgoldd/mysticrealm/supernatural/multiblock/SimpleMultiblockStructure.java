package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.resources.Identifier;

/**
 * Implementação padrão de {@link IMultiblockStructure} — apenas dados (id + pattern),
 * usada para registrar novas estruturas sem precisar criar uma classe por estrutura.
 */
public record SimpleMultiblockStructure(Identifier id, MultiblockPattern pattern, int tier) implements IMultiblockStructure {
}
