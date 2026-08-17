package com.aibots.client.renderer;

import com.aibots.Aibots;
import com.aibots.client.AibotsClient;
import com.aibots.client.model.MeouModel;
import com.aibots.entity.MeouEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MeouRenderer extends MobRenderer<MeouEntity, MeouModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/entity/meou.png");

    public MeouRenderer(EntityRendererProvider.Context context) {
        super(context, new MeouModel(context.bakeLayer(AibotsClient.MEOU_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MeouEntity entity) {
        return TEXTURE;
    }
}