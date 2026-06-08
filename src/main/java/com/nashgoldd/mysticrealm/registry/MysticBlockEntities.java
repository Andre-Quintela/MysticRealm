package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.block.entity.VampireObeliskBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class MysticBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, MysticRealm.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VampireObeliskBlockEntity>> VAMPIRE_OBELISK =
        BLOCK_ENTITY_TYPES.register("vampire_obelisk", () ->
            new BlockEntityType<>(VampireObeliskBlockEntity::new, Set.of(MysticBlocks.VAMPIRE_OBELISK.get()))
        );

    private MysticBlockEntities() {}

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
