package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

/**
 * Define uma estrutura multiblock registrável (ex.: Blood Altar, Sala do Trono, Portal das
 * Sombras). Genérico — não depende de raça/feature específica.
 */
public interface IMultiblockStructure {

    Identifier id();

    MultiblockPattern pattern();

    default MultiblockValidationResult validate(Level level, BlockPos center) {
        return MultiblockValidator.validate(level, center, pattern());
    }
}
