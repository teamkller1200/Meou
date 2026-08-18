package com.meou.screen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import com.meou.Meou;

/**
 * Server-side menu registration for the Meou inventory/skill UI.
 */
public final class ModMenuTypes {
    private ModMenuTypes() {
    }

    public static final MenuType<MeouScreenHandler> MEOU = Registry.register(
        BuiltInRegistries.MENU,
        Meou.id("meou"),
        new MenuType<>(MeouScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    public static void registerAll() {
    }
}