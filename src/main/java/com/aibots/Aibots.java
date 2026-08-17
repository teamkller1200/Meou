package com.aibots;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.entity.ModEntityTypes;
import com.aibots.screen.ModMenuTypes;

public class Aibots implements ModInitializer {
    public static final String MOD_ID = "aibots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        ModEntityTypes.registerAll();
        ModMenuTypes.registerAll();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}