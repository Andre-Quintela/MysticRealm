package com.nashgoldd.mysticrealm.supernatural.vampire.client.renderer;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.model.VampireEntityModel;
import com.nashgoldd.mysticrealm.supernatural.vampire.entity.HostileVampireEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
public class VampireEntityRenderer extends MobRenderer<HostileVampireEntity, HostileVampireRenderState, VampireEntityModel> {

    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(MysticRealm.MODID, "textures/entity/vampire_texture.png");

    public VampireEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new VampireEntityModel(context.bakeLayer(VampireEntityModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public Identifier getTextureLocation(HostileVampireRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public HostileVampireRenderState createRenderState() {
        return new HostileVampireRenderState();
    }

    @Override
    public void extractRenderState(HostileVampireEntity entity, HostileVampireRenderState renderState, float partialTick) {
        super.extractRenderState(entity, renderState, partialTick);
        renderState.idleAnimationState.copyFrom(entity.idleAnimationState);
        renderState.walkAnimationState.copyFrom(entity.walkAnimationState);
        renderState.attackAnimationState.copyFrom(entity.attackAnimationState);
    }
}
