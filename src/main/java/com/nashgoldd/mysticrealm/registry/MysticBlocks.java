package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.block.VampireObeliskBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MysticBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(MysticRealm.MODID);

    public static final DeferredBlock<VampireObeliskBlock> VAMPIRE_OBELISK =
        BLOCKS.registerBlock("vampire_obelisk", VampireObeliskBlock::new,
            p -> p.strength(3.5F, 1200F).noOcclusion().requiresCorrectToolForDrops());

    private MysticBlocks() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
