package com.nashgoldd.mysticrealm.supernatural.vampire.entity;

import com.nashgoldd.mysticrealm.config.MysticConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HostileVampireEntity extends Monster {

    private static final EntityDataAccessor<Boolean> DATA_IS_ATTACKING =
        SynchedEntityData.defineId(HostileVampireEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState   = new AnimationState();
    public final AnimationState walkAnimationState   = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    private int attackAnimationTimeout = 0;

    public HostileVampireEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,     50.0)
            .add(Attributes.ATTACK_DAMAGE,   5.0)
            .add(Attributes.MOVEMENT_SPEED,  0.32)
            .add(Attributes.FOLLOW_RANGE,   40.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_ATTACKING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            updateAnimations();
        } else {
            if (attackAnimationTimeout > 0) {
                attackAnimationTimeout--;
                if (attackAnimationTimeout == 0) {
                    this.entityData.set(DATA_IS_ATTACKING, false);
                }
            }
        }
    }

    private void updateAnimations() {
        boolean isAttacking = this.entityData.get(DATA_IS_ATTACKING);
        boolean isMoving    = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;

        if (isAttacking) {
            attackAnimationState.startIfStopped(this.tickCount);
            walkAnimationState.stop();
            idleAnimationState.stop();
        } else if (isMoving) {
            walkAnimationState.startIfStopped(this.tickCount);
            idleAnimationState.stop();
            attackAnimationState.stop();
        } else {
            idleAnimationState.startIfStopped(this.tickCount);
            walkAnimationState.stop();
            attackAnimationState.stop();
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(level, target);
        if (result) {
            this.entityData.set(DATA_IS_ATTACKING, true);
            this.attackAnimationTimeout = 20;
        }
        return result;
    }

    public static boolean checkSpawnRules(EntityType<HostileVampireEntity> type,
                                          ServerLevelAccessor level,
                                          EntitySpawnReason reason,
                                          BlockPos pos,
                                          RandomSource random) {
        if (!MysticConfig.ENABLE_VAMPIRE_SPAWN.get()) return false;
        return !level.getLevel().isBrightOutside()
            && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
