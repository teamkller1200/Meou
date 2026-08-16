package com.aibots.client;

import com.aibots.Aibots;
import com.aibots.client.model.TestEntityModel;
import com.aibots.client.renderer.TestEntityRenderer;
import com.aibots.entity.ModEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class AibotsClient implements ClientModInitializer {
    public static final ModelLayerLocation TEST_ENTITY_LAYER =
        new ModelLayerLocation(Aibots.id("test_entity"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(TEST_ENTITY_LAYER, TestEntityModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.TEST_ENTITY, TestEntityRenderer::new);
    }
}