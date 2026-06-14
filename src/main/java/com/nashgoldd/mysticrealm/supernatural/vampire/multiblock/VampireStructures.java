package com.nashgoldd.mysticrealm.supernatural.vampire.multiblock;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.multiblock.BlockMatcher;
import com.nashgoldd.mysticrealm.supernatural.multiblock.IMultiblockStructure;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockPattern;
import com.nashgoldd.mysticrealm.supernatural.multiblock.SimpleMultiblockStructure;
import com.nashgoldd.mysticrealm.supernatural.multiblock.StructureRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

/**
 * Estruturas multiblock específicas do vampirismo. Apenas dados (padrões) registrados no
 * {@link StructureRegistry} genérico — nenhuma lógica de validação vive aqui.
 */
public final class VampireStructures {

    public static final Identifier BLOOD_ALTAR_LVL1_ID =
        Identifier.fromNamespaceAndPath(MysticRealm.MODID, "blood_altar_lvl1");

    public static final IMultiblockStructure BLOOD_ALTAR_LVL1 = new SimpleMultiblockStructure(
        BLOOD_ALTAR_LVL1_ID,
        MultiblockPattern.builder()
            .block(-2, 0, -2, BlockMatcher.of(Blocks.RED_CANDLE))
            .block(2, 0, -2, BlockMatcher.of(Blocks.RED_CANDLE))
            .block(-2, 0, 2, BlockMatcher.of(Blocks.RED_CANDLE))
            .block(2, 0, 2, BlockMatcher.of(Blocks.RED_CANDLE))
            .block(3, 0, 0, BlockMatcher.of(Blocks.POLISHED_BLACKSTONE))
            .block(-3, 0, 0, BlockMatcher.of(Blocks.POLISHED_BLACKSTONE))
            .block(0, 0, 3, BlockMatcher.of(Blocks.POLISHED_BLACKSTONE))
            .block(0, 0, -3, BlockMatcher.of(Blocks.POLISHED_BLACKSTONE))
            .build()
    );

    private VampireStructures() {}

    public static void register() {
        StructureRegistry.register(BLOOD_ALTAR_LVL1);
    }
}
