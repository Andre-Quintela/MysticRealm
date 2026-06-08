package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MysticCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MysticRealm.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mysticrealm.main"))
                    .icon(() -> new ItemStack(MysticItems.VAMPIRE_BLOOD_VIAL.get()))
                    .displayItems((params, output) -> {
                        output.accept(MysticItems.VAMPIRE_OBELISK.get());
                        output.accept(MysticItems.BLOOD_VIAL.get());
                        output.accept(MysticItems.VAMPIRE_BLOOD_VIAL.get());
                        output.accept(MysticItems.WOODEN_STAKE.get());
                        output.accept(MysticItems.HOSTILE_VAMPIRE_SPAWN_EGG.get());
                    })
                    .build()
    );

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
