package com.aibots.item;

import com.aibots.Aibots;
import com.aibots.entity.ModEntityTypes;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
    public static final Item MEOU_SPAWN_EGG = new SpawnEggItem(
        ModEntityTypes.MEOU,
        0xFFFFFF, // 白（ベースカラー）
        0xE6C280, // クリーム/ライトブラウン（斑点カラー）
        new Item.Properties()
    );

    public static void registerAll() {
        Registry.register(BuiltInRegistries.ITEM, Aibots.id("meou_spawn_egg"), MEOU_SPAWN_EGG);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(MEOU_SPAWN_EGG);
        });
    }
}
