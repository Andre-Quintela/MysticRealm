package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Descrição de uma estrutura multiblock como uma lista de posições (offsets relativos ao centro)
 * e os blocos aceitos em cada uma. Construída via {@link #builder()} — sem ifs hardcoded.
 */
public final class MultiblockPattern {

    /**
     * @param optional se true, a posição é ignorada pelo {@link MultiblockValidator}
     *                 (ex.: correntes decorativas) mas ainda pode aparecer em resultados futuros.
     */
    public record PatternEntry(BlockPos offset, BlockMatcher matcher, boolean optional) {}

    private final List<PatternEntry> entries;

    private MultiblockPattern(List<PatternEntry> entries) {
        this.entries = entries;
    }

    public List<PatternEntry> entries() {
        return entries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<PatternEntry> entries = new ArrayList<>();

        private Builder() {}

        public Builder block(int x, int y, int z, BlockMatcher matcher) {
            entries.add(new PatternEntry(new BlockPos(x, y, z), matcher, false));
            return this;
        }

        public Builder optionalBlock(int x, int y, int z, BlockMatcher matcher) {
            entries.add(new PatternEntry(new BlockPos(x, y, z), matcher, true));
            return this;
        }

        public MultiblockPattern build() {
            return new MultiblockPattern(List.copyOf(entries));
        }
    }
}
