package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementada por {@link net.minecraft.world.level.block.entity.BlockEntity}s que são o centro
 * de uma {@link IMultiblockStructure} (ex.: VampireObeliskBlockEntity). Cuida do cache de
 * validação para evitar recalcular a estrutura a cada tick.
 */
public interface IMultiblockController {

    Identifier getStructureId();

    BlockPos getControllerPos();

    MultiblockValidationResult getCachedResult();

    void setCachedResult(MultiblockValidationResult result);

    boolean isStructureDirty();

    void markStructureDirty();

    /**
     * Força o recálculo da estrutura, ignorando o cache (usado quando o jogador ativa o altar).
     */
    default MultiblockValidationResult revalidate(Level level) {
        MultiblockValidationResult result = StructureRegistry.get(getStructureId())
            .map(structure -> structure.validate(level, getControllerPos()))
            .orElse(MultiblockValidationResult.EMPTY);
        setCachedResult(result);
        return result;
    }

    default MultiblockValidationResult getOrValidate(Level level) {
        if (isStructureDirty() || getCachedResult() == null) {
            return revalidate(level);
        }
        return getCachedResult();
    }

    /**
     * Offsets (relativos ao centro) que compõem o padrão atual — usado para decidir,
     * em {@code neighborChanged}, se um bloco vizinho alterado faz parte da estrutura.
     */
    default Set<BlockPos> getStructureOffsets() {
        return StructureRegistry.get(getStructureId())
            .map(structure -> structure.pattern().entries().stream()
                .map(MultiblockPattern.PatternEntry::offset)
                .collect(Collectors.toSet()))
            .orElse(Set.of());
    }
}
