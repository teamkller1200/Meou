package com.aibots.client;

import com.aibots.Aibots;
import com.aibots.client.model.AiluuModel;
import com.aibots.client.renderer.AiluuRenderer;
import com.aibots.entity.ModEntityTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class AibotsClient implements ClientModInitializer {
    public static final ModelLayerLocation AILUU_LAYER =
        new ModelLayerLocation(Aibots.id("ailuu"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(AILUU_LAYER, AiluuModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.AILUU, AiluuRenderer::new);
    }
}