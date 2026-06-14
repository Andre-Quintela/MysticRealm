package com.nashgoldd.mysticrealm.supernatural.multiblock.build;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Avança a {@link MultiblockBuildQueue} a cada tick do servidor.
 */
@EventBusSubscriber(modid = MysticRealm.MODID)
public final class MultiblockBuildTickHandler {

    private MultiblockBuildTickHandler() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MultiblockBuildQueue.tick();
    }
}
