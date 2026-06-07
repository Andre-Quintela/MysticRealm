package com.nashgoldd.mysticrealm;

import com.nashgoldd.mysticrealm.command.MysticCommands;
import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.event.handler.PlayerEventHandler;
import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.registry.MysticAttachments;
import com.nashgoldd.mysticrealm.registry.MysticEffects;
import com.nashgoldd.mysticrealm.registry.MysticEntityTypes;
import com.nashgoldd.mysticrealm.registry.MysticItems;
import com.nashgoldd.mysticrealm.registry.MysticParticles;
import com.nashgoldd.mysticrealm.supernatural.vampire.entity.HostileVampireEntity;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.handler.VampireEventHandler;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;

@Mod(MysticRealm.MODID)
public class MysticRealm {

    public static final String MODID = "mysticrealm";

    public MysticRealm(IEventBus modEventBus, ModContainer modContainer) {
        MysticAttachments.register(modEventBus);
        MysticEffects.register(modEventBus);
        MysticItems.register(modEventBus);
        MysticEntityTypes.register(modEventBus);
        MysticParticles.register(modEventBus);

        modEventBus.addListener(MysticNetwork::registerPayloads);
        modEventBus.addListener(this::onAttributeCreate);

        modContainer.registerConfig(ModConfig.Type.COMMON, MysticConfig.SPEC, "mysticrealm-common.toml");

        NeoForge.EVENT_BUS.register(new PlayerEventHandler());
        NeoForge.EVENT_BUS.register(new VampireEventHandler());
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        modEventBus.addListener(MysticRealm::onRegisterSpawnPlacements);

        MysticRealmLogger.info("MysticRealm inicializado — Fase 2 (Vampirismo)");
    }

    private void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(MysticEntityTypes.HOSTILE_VAMPIRE.get(),
            HostileVampireEntity.createAttributes().build());
    }

    private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
            MysticEntityTypes.HOSTILE_VAMPIRE.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            HostileVampireEntity::checkSpawnRules,
            RegisterSpawnPlacementsEvent.Operation.OR
        );
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MysticCommands.register(event.getDispatcher());
    }
}
