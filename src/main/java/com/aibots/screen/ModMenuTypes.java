package com.aibots.screen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import com.aibots.Aibots;

public final class ModMenuTypes {
    private ModMenuTypes() {
    }

    public static final MenuType<MeouScreenHandler> MEOU = Registry.register(
        BuiltInRegistries.MENU,
        Aibots.id("meou"),
        new MenuType<>(MeouScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    public static void registerAll() {
    }
}