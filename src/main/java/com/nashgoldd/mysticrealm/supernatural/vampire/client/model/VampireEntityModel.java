package com.nashgoldd.mysticrealm.supernatural.vampire.client.model;

import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.animation.VampireEntityAnimations;
import com.nashgoldd.mysticrealm.supernatural.vampire.client.renderer.HostileVampireRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier;
public class VampireEntityModel extends EntityModel<HostileVampireRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
        new ModelLayerLocation(Identifier.fromNamespaceAndPath(MysticRealm.MODID, "vampire"), "main");

    private final ModelPart head;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation meleeAttackAnimation;

    public VampireEntityModel(ModelPart root) {
        super(root);
        this.head                 = root.getChild("head");
        this.idleAnimation        = VampireEntityAnimations.idle.bake(root);
        this.walkAnimation        = VampireEntityAnimations.walk.bake(root);
        this.meleeAttackAnimation = VampireEntityAnimations.meleeAttack.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_fang_r1",
            CubeListBuilder.create()
                .texOffs(1, 33).addBox(-2.0F, 0.0F, -5.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 33).addBox( 1.0F, 0.0F, -5.0F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, -0.0436F, 0.0F, 0.0F));

        head.addOrReplaceChild("right_ear",
            CubeListBuilder.create()
                .texOffs(1, 0).addBox(-1.0F,  1.0F, -4.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F,  0.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F,  0.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F,  0.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F, -1.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F, -1.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 0).addBox(-1.0F, -2.0F,  0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offsetAndRotation(6.0F, -4.0F, 2.0F, 0.0F, 0.3054F, 0.0F));

        head.addOrReplaceChild("left_ear",
            CubeListBuilder.create()
                .texOffs(1, 0).mirror().addBox( 1.0F,  1.0F, -4.0F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F,  0.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F,  0.0F, -3.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F,  0.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F, -1.0F, -2.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F, -1.0F, -1.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 0).mirror().addBox( 1.0F, -2.0F,  0.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
            PartPose.offsetAndRotation(-6.0F, -4.0F, 2.0F, 0.0F, -0.3054F, 0.0F));

        partdefinition.addOrReplaceChild("body",
            CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
            CubeListBuilder.create()
                .texOffs(32, 16).addBox(-1.0F,  0.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs( 0, 36).addBox( 1.0F, 10.0F, -2.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs( 4, 36).addBox( 2.0F, 10.0F, -1.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs( 8, 36).addBox( 1.0F, 10.0F,  0.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create()
                .texOffs(48, 16).addBox(-3.0F,  0.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(12, 36).addBox(-2.0F, 10.0F, -2.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 36).addBox(-3.0F, 10.0F, -1.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 36).addBox(-2.0F, 10.0F,  0.0F, 1.0F,  3.0F, 1.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg",
            CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 12.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
            PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition right_wing_main = partdefinition.addOrReplaceChild("right_wing_main",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(-2.5F, 6.0F, 3.0F, 0.0F, 0.4363F, 0.0F));

        right_wing_main.addOrReplaceChild("outer_right_wing",
            CubeListBuilder.create().texOffs(42, 32).addBox(-6.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        right_wing_main.addOrReplaceChild("right_wing",
            CubeListBuilder.create().texOffs(38, 33).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 0.0F, 0.0F));

        PartDefinition left_wing_main = partdefinition.addOrReplaceChild("left_wing_main",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.5F, 6.0F, 2.0F, 0.0F, -0.4363F, 0.0F));

        left_wing_main.addOrReplaceChild("left_wing",
            CubeListBuilder.create().texOffs(37, 40).addBox(0.0F, -2.0F, 0.0F, 2.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 0.0F, 0.0F));

        left_wing_main.addOrReplaceChild("outer_left_wing",
            CubeListBuilder.create().texOffs(42, 40).addBox(0.0F, -2.0F, 0.0F, 6.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
            PartPose.offset(2.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(HostileVampireRenderState renderState) {
        super.setupAnim(renderState);
        this.idleAnimation.apply(renderState.idleAnimationState, renderState.ageInTicks);
        this.walkAnimation.apply(renderState.walkAnimationState, renderState.ageInTicks);
        this.meleeAttackAnimation.apply(renderState.attackAnimationState, renderState.ageInTicks);
        this.head.yRot = renderState.yRot * (float)(Math.PI / 180.0);
        this.head.xRot = renderState.xRot * (float)(Math.PI / 180.0);
    }
}
