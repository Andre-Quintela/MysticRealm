package com.nashgoldd.mysticrealm.supernatural.multiblock;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.supernatural.multiblock.build.MultiblockBuildJob;
import com.nashgoldd.mysticrealm.supernatural.multiblock.build.MultiblockBuildQueue;
import com.nashgoldd.mysticrealm.supernatural.multiblock.build.PendingPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tenta completar automaticamente as posições faltantes de uma {@link MultiblockPattern},
 * consumindo os blocos necessários do inventário do jogador e drenando vida como custo —
 * genérico, não depende da estrutura ou da raça do jogador.
 */
public final class MultiblockBuilder {

    private MultiblockBuilder() {}

    public static MultiblockBuildResult tryBuild(ServerLevel level, BlockPos center,
                                                   MultiblockPattern pattern, ServerPlayer player) {
        MultiblockValidationResult validation = MultiblockValidator.validate(level, center, pattern);
        if (validation.valid()) {
            return new MultiblockBuildResult(false, MultiblockBuildResult.Reason.ALREADY_COMPLETE,
                validation, List.of(), 0f);
        }

        Set<BlockPos> missing = new HashSet<>(validation.missingBlocks());
        List<PendingPlacement> placements = new ArrayList<>();
        for (MultiblockPattern.PatternEntry entry : pattern.entries()) {
            if (entry.optional()) continue;

            BlockPos pos = center.offset(entry.offset());
            if (!missing.contains(pos)) continue;

            entry.matcher().placementBlock().ifPresent(block -> placements.add(new PendingPlacement(pos, block)));
        }

        if (placements.isEmpty()) {
            return new MultiblockBuildResult(false, MultiblockBuildResult.Reason.ALREADY_COMPLETE,
                validation, List.of(), 0f);
        }

        Map<Item, Integer> required = new LinkedHashMap<>();
        for (PendingPlacement placement : placements) {
            required.merge(placement.block().asItem(), 1, Integer::sum);
        }

        SimpleContainer craftSlots = new SimpleContainer(0);
        List<ItemStack> missingItems = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            Item item = entry.getKey();
            int needed = entry.getValue();
            int have = player.getInventory().clearOrCountMatchingItems(
                stack -> stack.getItem() == item, 0, craftSlots);
            if (have < needed) {
                missingItems.add(new ItemStack(item, needed - have));
            }
        }

        if (!missingItems.isEmpty()) {
            return new MultiblockBuildResult(false, MultiblockBuildResult.Reason.MISSING_ITEMS,
                validation, missingItems, 0f);
        }

        float healthCost = placements.size() * MysticConfig.MULTIBLOCK_BUILD_HEALTH_COST_PER_BLOCK.get().floatValue();
        if (player.getHealth() <= healthCost) {
            return new MultiblockBuildResult(false, MultiblockBuildResult.Reason.INSUFFICIENT_HEALTH,
                validation, List.of(), healthCost);
        }

        for (Map.Entry<Item, Integer> entry : required.entrySet()) {
            Item item = entry.getKey();
            int needed = entry.getValue();
            player.getInventory().clearOrCountMatchingItems(stack -> stack.getItem() == item, needed, craftSlots);
        }

        player.hurtServer(level, level.damageSources().magic(), healthCost);

        // Os blocos são colocados ao longo do tempo (um a cada N ticks, com som de
        // colocação) por MultiblockBuildQueue, para dar a sensação de construção mágica.
        MultiblockBuildQueue.enqueue(new MultiblockBuildJob(level, center, pattern, player.getUUID(), placements));

        return new MultiblockBuildResult(true, MultiblockBuildResult.Reason.SUCCESS, validation, List.of(), healthCost);
    }
}
