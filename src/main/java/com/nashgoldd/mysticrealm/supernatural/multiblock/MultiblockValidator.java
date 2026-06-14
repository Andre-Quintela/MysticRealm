package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Valida um {@link MultiblockPattern} contra o mundo a partir de uma posição central,
 * sem manter estado — toda a lógica de cache/invalidação fica em {@link IMultiblockController}.
 */
public final class MultiblockValidator {

    private MultiblockValidator() {}

    public static MultiblockValidationResult validate(Level level, BlockPos center, MultiblockPattern pattern) {
        List<BlockPos> matched = new ArrayList<>();
        List<BlockPos> missing = new ArrayList<>();
        List<BlockPos> wrong = new ArrayList<>();
        int required = 0;

        for (MultiblockPattern.PatternEntry entry : pattern.entries()) {
            if (entry.optional()) continue;
            required++;

            BlockPos pos = center.offset(entry.offset());
            BlockState state = level.getBlockState(pos);

            if (entry.matcher().matches(state)) {
                matched.add(pos);
            } else if (state.isAir() || state.canBeReplaced()) {
                missing.add(pos);
            } else {
                wrong.add(pos);
            }
        }

        List<BlockPos> invalid = new ArrayList<>(missing.size() + wrong.size());
        invalid.addAll(missing);
        invalid.addAll(wrong);

        float percent = required == 0 ? 100f : (matched.size() * 100f) / required;

        return new MultiblockValidationResult(
            invalid.isEmpty(),
            List.copyOf(missing),
            List.copyOf(wrong),
            List.copyOf(invalid),
            List.copyOf(matched),
            percent
        );
    }
}
