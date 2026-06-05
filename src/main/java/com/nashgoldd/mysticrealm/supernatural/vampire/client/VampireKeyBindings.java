package com.nashgoldd.mysticrealm.supernatural.vampire.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nashgoldd.mysticrealm.MysticRealm;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class VampireKeyBindings {

    // Categoria "Mystic Realm" na tela de controles
    public static final KeyMapping.Category CATEGORY =
        new KeyMapping.Category(Identifier.fromNamespaceAndPath(MysticRealm.MODID, MysticRealm.MODID));

    public static final KeyMapping KEY_DRAIN_BLOOD = new KeyMapping(
        "key.mysticrealm.drain_blood",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    );

    private VampireKeyBindings() {}

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(KEY_DRAIN_BLOOD);
    }
}
