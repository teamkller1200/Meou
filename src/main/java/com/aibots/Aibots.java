package com.aibots;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aibots.entity.MeouEntity;
import com.aibots.entity.ModEntityTypes;
import com.aibots.entity.skill.MeouSkill;
import com.aibots.item.ModItems;
import com.aibots.screen.ModMenuTypes;
import com.aibots.screen.RenamePayload;
import com.aibots.screen.SkillSelectPayload;

public class Aibots implements ModInitializer {
    public static final String MOD_ID = "aibots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        ModEntityTypes.registerAll();
        ModItems.registerAll();
        ModMenuTypes.registerAll();
        registerPayloads();
    }

    private static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SkillSelectPayload.TYPE, SkillSelectPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RenamePayload.TYPE, RenamePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SkillSelectPayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> {
                if (context.player().level().getEntity(payload.entityId()) instanceof MeouEntity meou) {
                    MeouSkill skill = MeouSkill.byOrdinal(payload.skillOrdinal());
                    meou.setSelectedSkill(skill);
                    LOGGER.debug("[Meou {}] skill set to {}", payload.entityId(), skill.getKey());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.TYPE, (payload, context) -> {
            context.player().server.execute(() -> {
                if (context.player().level().getEntity(payload.entityId()) instanceof MeouEntity meou) {
                    String name = payload.name().trim();
                    if (!name.isEmpty()) {
                        meou.setCustomName(Component.literal(name));
                        meou.setCustomNameVisible(true);
                    }
                    LOGGER.debug("[Meou {}] renamed to {}", payload.entityId(), name);
                }
            });
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}