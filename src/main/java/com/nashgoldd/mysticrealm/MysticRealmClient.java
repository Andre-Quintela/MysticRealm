package com.nashgoldd.mysticrealm;

import com.nashgoldd.mysticrealm.registry.MysticEntityTypes;
import com.nashgoldd.mysticrealm.registry.MysticParticles;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.VampireKeyBindings;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.model.VampireEntityModel;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.particle.BloodDrainParticle;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.renderer.VampireEntityRenderer;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MysticRealm.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public class MysticRealmClient {

    public MysticRealmClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(VampireKeyBindings::register);
        modEventBus.addListener(MysticRealmClient::onRegisterRenderers);
        modEventBus.addListener(MysticRealmClient::onRegisterLayerDefinitions);
        modEventBus.addListener(MysticRealmClient::onRegisterParticles);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            MysticEntityTypes.HOSTILE_VAMPIRE.get(),
            VampireEntityRenderer::new
        );
    }

    private static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            VampireEntityModel.LAYER_LOCATION,
            VampireEntityModel::createBodyLayer
        );
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MysticParticles.BLOOD_DRAIN.get(), BloodDrainParticle.Provider::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MysticRealmLogger.info("MysticRealm Client carregado");
    }
}
