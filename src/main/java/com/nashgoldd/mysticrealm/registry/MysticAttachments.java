package com.nashgoldd.mysticrealm.registry;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.attachment.PlayerSupernaturalData;
import com.nashgoldd.mysticrealm.supernatural.ability.AbilityWheelData;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.EntityBloodData;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class MysticAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MysticRealm.MODID);

    public static final Supplier<AttachmentType<PlayerSupernaturalData>> SUPERNATURAL_DATA =
        ATTACHMENT_TYPES.register("supernatural_data", () ->
            AttachmentType.builder(() -> new PlayerSupernaturalData())
                .serialize(PlayerSupernaturalData.CODEC)
                .copyOnDeath()
                .build()
        );

    public static final Supplier<AttachmentType<VampireData>> VAMPIRE_DATA =
        ATTACHMENT_TYPES.register("vampire_data", () ->
            AttachmentType.builder(VampireData::new)
                .serialize(VampireData.CODEC)
                .copyOnDeath()
                .build()
        );

    public static final Supplier<AttachmentType<EntityBloodData>> ENTITY_BLOOD =
        ATTACHMENT_TYPES.register("entity_blood", () ->
            AttachmentType.builder(EntityBloodData::new)
                .serialize(EntityBloodData.CODEC)
                .build()
        );

    public static final Supplier<AttachmentType<AbilityWheelData>> ABILITY_WHEEL =
        ATTACHMENT_TYPES.register("ability_wheel", () ->
            AttachmentType.builder(AbilityWheelData::new)
                .serialize(AbilityWheelData.CODEC)
                .copyOnDeath()
                .build()
        );

    private MysticAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
