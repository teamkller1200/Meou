package com.meou.entity;

import com.meou.Meou;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registry for all Meou entity definitions.
 */
public final class ModEntityTypes {
    private ModEntityTypes() {
    }

    public static final EntityType<MeouEntity> MEOU = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(Meou.MOD_ID, "meou"),
        EntityType.Builder.of(MeouEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.8F)
            .build("meou")
    );

    public static void registerAll() {
        FabricDefaultAttributeRegistry.register(MEOU, MeouEntity.createMobAttributes());
    }
}