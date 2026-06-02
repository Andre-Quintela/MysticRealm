package com.nashgoldd.mysticrealm.supernatural.vampire.registry;

import com.nashgoldd.mysticrealm.network.MysticDamageTypes;
import com.nashgoldd.mysticrealm.supernatural.vampire.item.WoodenStakeItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Determina quais fontes de dano são letais para vampiros.
 *
 * Danos não letais (queda, fogo, lava, explosão, mobs, etc.) são interceptados pela
 * imortalidade vampírica. Apenas fraquezas sobrenaturais (luz solar, estaca de madeira,
 * dano sagrado, prata) podem matar vampiros normalmente.
 */
public final class VampireWeaknessRegistry {

    private VampireWeaknessRegistry() {}

    public static boolean isLethalToVampire(DamageSource source) {
        // SUNLIGHT: dano do tipo customizado mysticrealm:sunlight
        if (source.is(MysticDamageTypes.SUNLIGHT)) {
            return true;
        }

        // WOODEN_STAKE: atacante segura uma WoodenStakeItem na mão principal
        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof LivingEntity attacker) {
            if (attacker.getMainHandItem().getItem() instanceof WoodenStakeItem) {
                return true;
            }
        }

        // SILVER e HOLY_DAMAGE: infraestrutura preparada para expansão futura
        // Exemplo futuro: if (source.is(MysticDamageTypes.SILVER)) return true;

        return false;
    }
}
