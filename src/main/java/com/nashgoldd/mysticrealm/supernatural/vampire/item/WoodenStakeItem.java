package com.nashgoldd.mysticrealm.supernatural.vampire.item;

import net.minecraft.world.item.Item;

/**
 * Estaca de Madeira — fraqueza vampírica letal.
 *
 * A detecção ocorre em {@link com.nashgoldd.mysticrealm.supernatural.vampire.registry.VampireWeaknessRegistry}
 * via inspeção do item na mão do atacante durante {@link com.nashgoldd.mysticrealm.supernatural.vampire.event.handler.VampireEventHandler#onLivingDeath}.
 * Nenhuma lógica de Mixin é necessária.
 */
public class WoodenStakeItem extends Item {

    public WoodenStakeItem(Properties properties) {
        super(properties);
    }
}
