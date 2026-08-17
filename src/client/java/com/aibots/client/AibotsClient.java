package com.aibots.client;

import com.aibots.Aibots;
import com.aibots.client.model.MeouModel;
import com.aibots.client.renderer.MeouRenderer;
import com.aibots.client.screen.MeouScreen;
import com.aibots.entity.ModEntityTypes;
import com.aibots.screen.ModMenuTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class AibotsClient implements ClientModInitializer {
    public static final ModelLayerLocation MEOU_LAYER =
        new ModelLayerLocation(Aibots.id("meou"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MEOU_LAYER, MeouModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.MEOU, MeouRenderer::new);
        MenuScreens.register(ModMenuTypes.MEOU, MeouScreen::new);
    }
}