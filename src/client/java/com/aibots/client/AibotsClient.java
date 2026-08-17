package com.aibots.client;

import com.aibots.Aibots;
import com.aibots.client.model.AiluuModel;
import com.aibots.client.renderer.AiluuRenderer;
import com.aibots.client.screen.AiluuScreen;
import com.aibots.entity.ModEntityTypes;
import com.aibots.screen.ModMenuTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class AibotsClient implements ClientModInitializer {
    public static final ModelLayerLocation AILUU_LAYER =
        new ModelLayerLocation(Aibots.id("ailuu"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(AILUU_LAYER, AiluuModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.AILUU, AiluuRenderer::new);
        MenuScreens.register(ModMenuTypes.AILUU, AiluuScreen::new);
    }
}