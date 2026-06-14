package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.Set;

/**
 * Critério de aceitação de um {@link BlockState} para uma posição de um {@link MultiblockPattern}.
 * Implementações comuns: {@link #of(Block...)} (lista de blocos equivalentes) e {@link #tag(TagKey)}.
 */
public interface BlockMatcher {

    boolean matches(BlockState state);

    /**
     * Bloco "canônico" a ser colocado por {@code MultiblockBuilder} quando esta posição estiver
     * faltando. {@link Optional#empty()} significa que a posição não é auto-construível
     * (ex.: matchers baseados em tag, sem um bloco único representativo).
     */
    default Optional<Block> placementBlock() {
        return Optional.empty();
    }

    static BlockMatcher of(Block... blocks) {
        Set<Block> accepted = Set.of(blocks);
        Block placement = blocks[0];
        return new BlockMatcher() {
            @Override
            public boolean matches(BlockState state) {
                return accepted.contains(state.getBlock());
            }

            @Override
            public Optional<Block> placementBlock() {
                return Optional.of(placement);
            }
        };
    }

    static BlockMatcher tag(TagKey<Block> tag) {
        return state -> state.is(tag);
    }
}
