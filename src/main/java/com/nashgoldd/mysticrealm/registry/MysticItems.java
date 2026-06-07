package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.item.BloodVialItem;
import com.nashgoldd.mysticrealm.supernatural.vampire.item.VampireBloodVialItem;
import com.nashgoldd.mysticrealm.supernatural.vampire.item.WoodenStakeItem;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MysticItems {

    public static final DeferredRegister.Items ITEMS =
        DeferredRegister.createItems(MysticRealm.MODID);

    public static final DeferredItem<WoodenStakeItem> WOODEN_STAKE =
        ITEMS.registerItem("wooden_stake", WoodenStakeItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<BloodVialItem> BLOOD_VIAL =
        ITEMS.registerItem("blood_vial", BloodVialItem::new, p -> p.stacksTo(16));

    public static final DeferredItem<VampireBloodVialItem> VAMPIRE_BLOOD_VIAL =
        ITEMS.registerItem("vampire_blood_vial", VampireBloodVialItem::new, p -> p.stacksTo(16));

    public static final DeferredItem<SpawnEggItem> HOSTILE_VAMPIRE_SPAWN_EGG =
        ITEMS.registerItem("hostile_vampire_spawn_egg",
            SpawnEggItem::new,
            p -> p.stacksTo(64).spawnEgg(MysticEntityTypes.HOSTILE_VAMPIRE.get()));

    private MysticItems() {}

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
