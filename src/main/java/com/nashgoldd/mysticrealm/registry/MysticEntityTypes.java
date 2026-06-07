package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.entity.HostileVampireEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MysticEntityTypes {

    public static final DeferredRegister.Entities ENTITY_TYPES =
        DeferredRegister.createEntities(MysticRealm.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HostileVampireEntity>> HOSTILE_VAMPIRE =
        ENTITY_TYPES.registerEntityType("hostile_vampire", HostileVampireEntity::new, MobCategory.MONSTER,
            builder -> builder.sized(0.6F, 1.95F).clientTrackingRange(8));

    private MysticEntityTypes() {}

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
