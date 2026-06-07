package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MysticParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, MysticRealm.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_DRAIN =
        PARTICLE_TYPES.register("blood_drain", () -> new SimpleParticleType(false));

    public static void register(IEventBus modEventBus) {
        PARTICLE_TYPES.register(modEventBus);
    }
}
