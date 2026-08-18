package com.meou.client.renderer;

import com.meou.Meou;
import com.meou.client.MeouClient;
import com.meou.client.model.MeouModel;
import com.meou.entity.MeouEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders the Meou model and adds the item-in-hand layer used by the attack skill.
 */
public class MeouRenderer extends MobRenderer<MeouEntity, MeouModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Meou.MOD_ID, "textures/entity/meou.png");

    public MeouRenderer(EntityRendererProvider.Context context) {
        super(context, new MeouModel(context.bakeLayer(MeouClient.MEOU_LAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(MeouEntity entity) {
        return TEXTURE;
    }
}