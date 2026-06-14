package com.nashgoldd.mysticrealm.supernatural.vampire.block.entity;

import com.nashgoldd.mysticrealm.registry.MysticBlockEntities;
import com.nashgoldd.mysticrealm.supernatural.multiblock.IMultiblockController;
import com.nashgoldd.mysticrealm.supernatural.multiblock.MultiblockValidationResult;
import com.nashgoldd.mysticrealm.supernatural.vampire.multiblock.VampireStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

public class VampireObeliskBlockEntity extends BlockEntity implements GeoBlockEntity, IMultiblockController {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.vampire_obelisk.idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private MultiblockValidationResult cachedResult = MultiblockValidationResult.EMPTY;
    private boolean structureDirty = true;

    public VampireObeliskBlockEntity(BlockPos pos, BlockState state) {
        super(MysticBlockEntities.VAMPIRE_OBELISK.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<VampireObeliskBlockEntity>("idle_controller", 0,
            animTest -> animTest.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public Identifier getStructureId() {
        return VampireStructures.BLOOD_ALTAR_LVL1_ID;
    }

    @Override
    public BlockPos getControllerPos() {
        return getBlockPos();
    }

    @Override
    public MultiblockValidationResult getCachedResult() {
        return cachedResult;
    }

    @Override
    public void setCachedResult(MultiblockValidationResult result) {
        this.cachedResult = result;
        this.structureDirty = false;
    }

    @Override
    public boolean isStructureDirty() {
        return structureDirty;
    }

    @Override
    public void markStructureDirty() {
        this.structureDirty = true;
    }
}
