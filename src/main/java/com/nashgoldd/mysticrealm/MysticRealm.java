package com.nashgoldd.mysticrealm;

import com.nashgoldd.mysticrealm.command.MysticCommands;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.event.handler.PlayerEventHandler;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.registry.MysticItems;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.handler.VampireEventHandler;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(MysticRealm.MODID)
public class MysticRealm {

    public static final String MODID = "mysticrealm";

    public MysticRealm(IEventBus modEventBus, ModContainer modContainer) {
        MysticAttachments.register(modEventBus);
        MysticItems.register(modEventBus);

        modEventBus.addListener(MysticNetwork::registerPayloads);

        modContainer.registerConfig(ModConfig.Type.COMMON, MysticConfig.SPEC, "mysticrealm-common.toml");

        NeoForge.EVENT_BUS.register(new PlayerEventHandler());
        NeoForge.EVENT_BUS.register(new VampireEventHandler());
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        MysticRealmLogger.info("MysticRealm inicializado — Fase 2 (Vampirismo)");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MysticCommands.register(event.getDispatcher());
    }
}
