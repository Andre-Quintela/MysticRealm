package com.nashgoldd.mysticrealm.supernatural.vampire.item;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import com.nashgoldd.mysticrealm.registry.MysticEffects;
import com.nashgoldd.mysticrealm.supernatural.vampire.event.VampireInfectionStartEvent;
import com.nashgoldd.mysticrealm.supernatural.vampire.service.VampireService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

public class VampireBloodVialItem extends Item {

    private static final int USE_DURATION = 32;

    public VampireBloodVialItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (VampireService.isVampire(player)) return InteractionResult.PASS;
        if (player.hasEffect(MysticEffects.VAMPIRE_INFECTION)) return InteractionResult.PASS;

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return stack;
        if (VampireService.isVampire(player)) return stack;

        int durationTicks = MysticConfig.INFECTION_DURATION_SECONDS.get() * 20;
        player.addEffect(new MobEffectInstance(MysticEffects.VAMPIRE_INFECTION, durationTicks, 0, false, true));

        NeoForge.EVENT_BUS.post(new VampireInfectionStartEvent(player));

        player.sendSystemMessage(Component.translatable("mysticrealm.vampire.infection.start"));
        stack.shrink(1);
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
