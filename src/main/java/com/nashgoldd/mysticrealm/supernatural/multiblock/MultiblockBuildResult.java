package com.nashgoldd.mysticrealm.supernatural.multiblock;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Resultado de uma tentativa de construção automática de uma {@link MultiblockPattern}
 * via {@link MultiblockBuilder#tryBuild}.
 */
public record MultiblockBuildResult(
    boolean success,
    Reason reason,
    MultiblockValidationResult validation,
    List<ItemStack> missingItems,
    float healthCost
) {
    public enum Reason {
        SUCCESS,
        ALREADY_COMPLETE,
        MISSING_ITEMS,
        INSUFFICIENT_HEALTH
    }
}
