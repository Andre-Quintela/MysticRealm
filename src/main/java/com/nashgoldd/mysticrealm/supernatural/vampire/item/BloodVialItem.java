package com.nashgoldd.mysticrealm.supernatural.vampire.item;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * Ampola de Sangue — restaura 25% de sangue ao ser consumida por um vampiro.
 * Internamente adiciona 5 food units (25% de 20 = 5).
 */
public class BloodVialItem extends Item {

    private static final int BLOOD_RESTORE_PERCENT = 25; // percentual exibido ao jogador
    private static final int FOOD_RESTORE = BLOOD_RESTORE_PERCENT / 5; // = 5 food units
    private static final int USE_DURATION = 32;

    public BloodVialItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!VampireService.isVampire(player)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player && VampireService.isVampire(player)) {
            FoodData food = player.getFoodData();
            food.setFoodLevel(Math.min(20, food.getFoodLevel() + FOOD_RESTORE));
            MysticNetwork.syncVampireToClient(player);
            int bloodPercent = food.getFoodLevel() * 5;
            player.sendSystemMessage(Component.literal(
                "§4[Blood]§r +" + BLOOD_RESTORE_PERCENT + "% → " + bloodPercent + "/100"
            ));
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }
}
