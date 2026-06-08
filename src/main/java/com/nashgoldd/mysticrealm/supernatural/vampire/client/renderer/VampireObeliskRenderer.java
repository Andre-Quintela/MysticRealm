package com.nashgoldd.mysticrealm.supernatural.vampire.client.renderer;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.nashgoldd.mysticrealm.MysticRealm;
import com.nashgoldd.mysticrealm.supernatural.vampire.block.entity.VampireObeliskBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;

public class VampireObeliskRenderer extends GeoBlockRenderer<VampireObeliskBlockEntity, BlockEntityRenderState> {

    public VampireObeliskRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(
            Identifier.fromNamespaceAndPath(MysticRealm.MODID, "vampire_obelisk")));
    }
}
