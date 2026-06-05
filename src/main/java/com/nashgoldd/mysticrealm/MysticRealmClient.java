package com.nashgoldd.mysticrealm;

import com.nashgoldd.mysticrealm.supernatural.vampire.client.VampireKeyBindings;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MysticRealm.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public class MysticRealmClient {

    public MysticRealmClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // Registrar keybinds vampíricos no MOD event bus
        modEventBus.addListener(VampireKeyBindings::register);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MysticRealmLogger.info("MysticRealm Client carregado");
    }
}
