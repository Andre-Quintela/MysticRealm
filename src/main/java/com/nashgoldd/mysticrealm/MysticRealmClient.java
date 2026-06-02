package com.nashgoldd.mysticrealm;

import com.nashgoldd.mysticrealm.network.ClientPacketHandlers;
import com.nashgoldd.mysticrealm.network.SyncPlayerDataPacket;
import com.nashgoldd.mysticrealm.util.MysticRealmLogger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Ponto de entrada client-only do MysticRealm.
 * Carregado apenas no cliente físico (não em servidores dedicados).
 *
 * ClientPacketHandlers é referenciado aqui por conveniência de organização,
 * mas o handler de SyncPlayerDataPacket é registrado em MysticNetwork via
 * method reference lazy (ClientPacketHandlers::handleSyncPlayerData).
 */
@Mod(value = MysticRealm.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MysticRealm.MODID, value = Dist.CLIENT)
public class MysticRealmClient {

    public MysticRealmClient(ModContainer container) {
        // Habilita a tela de configuração via menu Mods → MysticRealm → Config
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        MysticRealmLogger.info("MysticRealm Client carregado");
    }
}
