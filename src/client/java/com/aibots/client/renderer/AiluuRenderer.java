package com.aibots.client.renderer;

import com.aibots.Aibots;
import com.aibots.client.AibotsClient;
import com.aibots.client.model.AiluuModel;
import com.aibots.entity.AiluuEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AiluuRenderer extends MobRenderer<AiluuEntity, AiluuModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/entity/ailuu.png");

    public AiluuRenderer(EntityRendererProvider.Context context) {
        super(context, new AiluuModel(context.bakeLayer(AibotsClient.AILUU_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AiluuEntity entity) {
        return TEXTURE;
    }
}