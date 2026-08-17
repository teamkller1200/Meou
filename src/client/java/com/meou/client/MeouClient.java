package com.meou.client;

import com.meou.Meou;
import com.meou.client.model.MeouModel;
import com.meou.client.renderer.MeouRenderer;
import com.meou.client.screen.MeouScreen;
import com.meou.entity.ModEntityTypes;
import com.meou.screen.ModMenuTypes;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class MeouClient implements ClientModInitializer {
    public static final ModelLayerLocation MEOU_LAYER =
        new ModelLayerLocation(Meou.id("meou"), "main");

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(MEOU_LAYER, MeouModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntityTypes.MEOU, MeouRenderer::new);
        MenuScreens.register(ModMenuTypes.MEOU, MeouScreen::new);
    }
}