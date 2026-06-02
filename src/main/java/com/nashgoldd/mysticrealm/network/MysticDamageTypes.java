package com.nashgoldd.mysticrealm.network;

import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class MysticDamageTypes {

    public static final ResourceKey<DamageType> SUNLIGHT = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath(MysticRealm.MODID, "sunlight")
    );

    private MysticDamageTypes() {}
}
