package com.nashgoldd.mysticrealm.supernatural.vampire.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;
public class HostileVampireRenderState extends LivingEntityRenderState {
    public final AnimationState idleAnimationState   = new AnimationState();
    public final AnimationState walkAnimationState   = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}
