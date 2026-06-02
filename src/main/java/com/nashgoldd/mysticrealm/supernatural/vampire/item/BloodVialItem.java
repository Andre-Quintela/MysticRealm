package com.nashgoldd.mysticrealm.supernatural.vampire.item;

import com.nashgoldd.mysticrealm.network.MysticNetwork;
import com.nashgoldd.mysticrealm.supernatural.vampire.attachment.VampireData;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

/**
 * Ampola de Sangue — restaura 25 de sangue ao ser consumida por um vampiro.
 */
public class BloodVialItem extends Item {

    private static final int BLOOD_RESTORE = 25;
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
            VampireData data = VampireService.getData(player);
            data.addBlood(BLOOD_RESTORE, player);
            MysticNetwork.syncVampireToClient(player);
            player.sendSystemMessage(Component.literal(
                "§4[Blood]§r +" + BLOOD_RESTORE + " → " + data.getBloodLevel() + "/" + data.getMaxBlood()
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
