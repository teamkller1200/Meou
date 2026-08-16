package com.aibots.entity;

import com.aibots.Aibots;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {
    private ModEntityTypes() {
    }

    public static final EntityType<TestEntity> TEST_ENTITY = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(Aibots.MOD_ID, "test_entity"),
        FabricEntityTypeBuilder.create(MobCategory.CREATURE, TestEntity::new)
            .dimensions(EntityDimensions.scalable(0.6F, 1.8F))
            .build()
    );

    public static void registerAll() {
        FabricDefaultAttributeRegistry.register(TEST_ENTITY, TestEntity.createMobAttributes());
    }
}