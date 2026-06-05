package com.nashgoldd.mysticrealm.supernatural.vampire.feeding;

import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

public final class DrainableEntityRegistry {

    // EntityType constants são estáveis entre versões; evita problemas com renomeação de classes
    private static final Set<EntityType<?>> VALID_NPC_TYPES = Set.of(
        EntityType.VILLAGER,
        EntityType.WANDERING_TRADER
    );

    private DrainableEntityRegistry() {}

    public static boolean isValidTarget(LivingEntity target, Player vampire) {
        if (target == null || target.isDeadOrDying()) return false;
        if (target == vampire) return false;

        if (target instanceof Player p) {
            if (VampireService.isVampire(p)) return false;
            if (p.isCreative() || p.isSpectator()) return false;
            return true;
        }

        // Animal abrange vaca, porco, ovelha, galinha, cavalo, etc.
        if (target instanceof Animal) return true;

        // Aldeões e comerciantes errantes por tipo (não extendem Animal)
        return VALID_NPC_TYPES.contains(target.getType());
    }
}
