package com.aibots.client.renderer;

import com.aibots.Aibots;
import com.aibots.client.AibotsClient;
import com.aibots.client.model.TestEntityModel;
import com.aibots.entity.TestEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TestEntityRenderer extends MobRenderer<TestEntity, TestEntityModel> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "textures/entity/test_entity.png");

    public TestEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TestEntityModel(context.bakeLayer(AibotsClient.TEST_ENTITY_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TestEntity entity) {
        return TEXTURE;
    }
}