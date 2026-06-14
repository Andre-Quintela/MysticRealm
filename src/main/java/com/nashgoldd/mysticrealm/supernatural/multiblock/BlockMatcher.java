package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Critério de aceitação de um {@link BlockState} para uma posição de um {@link MultiblockPattern}.
 * Implementações comuns: {@link #of(Block...)} (lista de blocos equivalentes) e {@link #tag(TagKey)}.
 */
public interface BlockMatcher {

    boolean matches(BlockState state);

    static BlockMatcher of(Block... blocks) {
        Set<Block> accepted = Set.of(blocks);
        return state -> accepted.contains(state.getBlock());
    }

    static BlockMatcher tag(TagKey<Block> tag) {
        return state -> state.is(tag);
    }
}
